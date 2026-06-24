package com.taufik.managemoney.data.ai

import android.util.Log
import com.taufik.managemoney.domain.ai.FinancialAnalyst
import com.taufik.managemoney.domain.model.AggregatedFinance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/** Maks. putaran tool calling per pertanyaan (cegah loop tak berujung / boros). */
private const val MAX_TOOL_TURNS = 6

private const val TAG = "TanyaAI"

/**
 * Analis keuangan AI. Reuse adapter & failover yang sama seperti parser.
 * - [summarize] = insight naratif dari agregat (single-shot, failover antar provider).
 * - [ask] = Tanya AI dengan FUNCTION CALLING: model meminta data via tools ([ChatTools]) seperlunya,
 *   lalu menjawab. Berjalan pada satu provider aktif (loop multi-turn tak bisa lintas provider).
 */
@Singleton
internal class FinancialAnalystImpl @Inject constructor(
    private val geminiApi: GeminiApi,
    private val openAiApi: OpenAiApi,
    private val configStore: AiConfigStore,
    private val chatTools: ChatTools,
    private val json: Json,
) : FinancialAnalyst {

    override suspend fun summarize(data: AggregatedFinance): Result<String> =
        run(ANALYSIS_SYSTEM_INSTRUCTION, buildAnalysisUserContent(data))

    override suspend fun ask(question: String): Result<String> {
        val cfg = configStore.parseCandidates().firstOrNull { it.apiKey.isNotBlank() }
            ?: return Result.failure(ParseException("Belum ada provider AI dengan API key."))
        val system = buildAskSystem()
        Log.d(TAG, "ask provider=${cfg.provider} model=${cfg.model} q=$question")
        return runCatching {
            if (cfg.provider.openAiCompatible) askOpenAi(cfg, system, question)
            else askGemini(cfg, system, question)
        }.onFailure { Log.w(TAG, "ask gagal: ${it.message}", it) }
    }

    private fun buildAskSystem(): String {
        val today = LocalDate.now(ZoneId.of("Asia/Jakarta"))
        return "$ASK_SYSTEM_INSTRUCTION\nHari ini: $today (Asia/Jakarta)."
    }

    // --- Tanya AI: loop tool calling per provider ---

    private suspend fun askGemini(cfg: AiConfig, system: String, question: String): String {
        val contents = ArrayList<GeminiContent>()
        contents += GeminiContent(role = "user", parts = listOf(GeminiPart(text = question)))
        val tools = listOf(GeminiTool(geminiFunctionDeclarations()))

        repeat(MAX_TOOL_TURNS) {
            val body = GeminiRequest(
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = system))),
                contents = contents,
                generationConfig = GenerationConfig(responseMimeType = "text/plain", temperature = 0.3),
                tools = tools,
            )
            val resp = withContext(Dispatchers.IO) { geminiApi.generate(cfg.model, cfg.apiKey, body) }
            val content = resp.candidates.firstOrNull()?.content
                ?: throw ParseException("Balasan Gemini kosong")
            val calls = content.parts.mapNotNull { it.functionCall }
            Log.d(TAG, "gemini turn: calls=${calls.map { "${it.name}${it.args}" }}")

            if (calls.isEmpty()) {
                val text = content.parts.firstNotNullOfOrNull { it.text }?.trim()
                if (!text.isNullOrBlank()) return text
                throw ParseException("Balasan Gemini kosong")
            }

            contents += content // echo giliran model (berisi functionCall)
            contents += GeminiContent(
                role = "user",
                parts = calls.map { call ->
                    val result = chatTools.dispatch(call.name, call.args)
                    Log.d(TAG, "tool ${call.name} -> ${result.toString().take(300)}")
                    GeminiPart(functionResponse = GeminiFunctionResponse(name = call.name, response = result))
                },
            )
        }
        throw ParseException("Terlalu banyak langkah; coba pertanyaan lebih spesifik.")
    }

    private suspend fun askOpenAi(cfg: AiConfig, system: String, question: String): String {
        val messages = ArrayList<ChatMessage>()
        messages += ChatMessage(role = "system", content = system)
        messages += ChatMessage(role = "user", content = question)
        val tools = openAiTools()

        repeat(MAX_TOOL_TURNS) {
            val body = ChatRequest(
                model = cfg.model,
                messages = messages,
                temperature = 0.3,
                tools = tools,
                toolChoice = "auto",
            )
            val resp = withContext(Dispatchers.IO) { openAiApi.chat(cfg.baseUrl, "Bearer ${cfg.apiKey}", body) }
            val msg = resp.choices.firstOrNull()?.message ?: throw ParseException("Balasan AI kosong")
            val calls = msg.toolCalls
            Log.d(TAG, "openai turn: calls=${calls?.map { "${it.function.name}(${it.function.arguments})" }}")

            if (calls.isNullOrEmpty()) {
                val text = msg.content?.trim()
                if (!text.isNullOrBlank()) return text
                throw ParseException("Balasan AI kosong")
            }

            messages += msg // echo giliran assistant (berisi tool_calls)
            calls.forEach { call ->
                val result = chatTools.dispatch(call.function.name, parseArgs(call.function.arguments))
                Log.d(TAG, "tool ${call.function.name} -> ${result.toString().take(300)}")
                messages += ChatMessage(role = "tool", toolCallId = call.id, content = result.toString())
            }
        }
        throw ParseException("Terlalu banyak langkah; coba pertanyaan lebih spesifik.")
    }

    private fun parseArgs(raw: String): JsonObject =
        if (raw.isBlank()) JsonObject(emptyMap())
        else runCatching { json.parseToJsonElement(raw).jsonObject }.getOrDefault(JsonObject(emptyMap()))

    // --- Insight (summarize): failover provider aktif → provider lain ber-key, sampai sukses ---

    private suspend fun run(system: String, user: String): Result<String> {
        val candidates = configStore.parseCandidates()
        var last: Throwable? = null
        for (cfg in candidates) {
            if (cfg.apiKey.isBlank()) continue
            val res = runCatching {
                if (cfg.provider.openAiCompatible) callOpenAi(cfg, system, user) else callGemini(cfg, system, user)
            }
            val text = res.getOrNull()
            if (res.isSuccess && !text.isNullOrBlank()) return Result.success(text.trim())
            last = res.exceptionOrNull() ?: last
        }
        return Result.failure(ParseException(last?.message ?: "Belum ada provider AI dengan API key."))
    }

    private suspend fun callGemini(cfg: AiConfig, system: String, user: String): String = withContext(Dispatchers.IO) {
        val body = GeminiRequest(
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(system))),
            contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(user)))),
            generationConfig = GenerationConfig(responseMimeType = "text/plain", temperature = 0.4),
        )
        val resp = geminiApi.generate(cfg.model, cfg.apiKey, body)
        resp.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
    }

    private suspend fun callOpenAi(cfg: AiConfig, system: String, user: String): String = withContext(Dispatchers.IO) {
        val body = ChatRequest(
            model = cfg.model,
            messages = listOf(
                ChatMessage(role = "system", content = system),
                ChatMessage(role = "user", content = user),
            ),
            temperature = 0.4,
        )
        val resp = openAiApi.chat(cfg.baseUrl, "Bearer ${cfg.apiKey}", body)
        resp.choices.firstOrNull()?.message?.content.orEmpty()
    }
}
