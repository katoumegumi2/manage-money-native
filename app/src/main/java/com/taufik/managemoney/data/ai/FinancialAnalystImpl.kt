package com.taufik.managemoney.data.ai

import com.taufik.managemoney.domain.ai.FinancialAnalyst
import com.taufik.managemoney.domain.model.AggregatedFinance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analis keuangan AI. Reuse adapter & failover yang sama seperti parser: coba provider aktif
 * dulu, lalu provider lain yang punya key. Output = teks naratif (bukan JSON schema parser).
 */
@Singleton
internal class FinancialAnalystImpl @Inject constructor(
    private val geminiApi: GeminiApi,
    private val openAiApi: OpenAiApi,
    private val configStore: AiConfigStore,
) : FinancialAnalyst {

    override suspend fun summarize(data: AggregatedFinance): Result<String> {
        val user = buildAnalysisUserContent(data)
        val candidates = configStore.parseCandidates()
        var last: Throwable? = null
        for (cfg in candidates) {
            if (cfg.apiKey.isBlank()) continue
            val res = runCatching {
                if (cfg.provider.openAiCompatible) callOpenAi(cfg, user) else callGemini(cfg, user)
            }
            val text = res.getOrNull()
            if (res.isSuccess && !text.isNullOrBlank()) return Result.success(text.trim())
            last = res.exceptionOrNull() ?: last
        }
        return Result.failure(ParseException(last?.message ?: "Belum ada provider AI dengan API key."))
    }

    private suspend fun callGemini(cfg: AiConfig, user: String): String = withContext(Dispatchers.IO) {
        val body = GeminiRequest(
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(ANALYSIS_SYSTEM_INSTRUCTION))),
            contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(user)))),
            generationConfig = GenerationConfig(responseMimeType = "text/plain", temperature = 0.4),
        )
        val resp = geminiApi.generate(cfg.model, cfg.apiKey, body)
        resp.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
    }

    private suspend fun callOpenAi(cfg: AiConfig, user: String): String = withContext(Dispatchers.IO) {
        val body = ChatRequest(
            model = cfg.model,
            messages = listOf(
                ChatMessage(role = "system", content = ANALYSIS_SYSTEM_INSTRUCTION),
                ChatMessage(role = "user", content = user),
            ),
            temperature = 0.4,
        )
        val resp = openAiApi.chat(cfg.baseUrl, "Bearer ${cfg.apiKey}", body)
        resp.choices.firstOrNull()?.message?.content.orEmpty()
    }
}
