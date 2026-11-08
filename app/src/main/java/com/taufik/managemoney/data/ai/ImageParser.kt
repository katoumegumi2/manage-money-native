package com.taufik.managemoney.data.ai

import android.util.Log
import com.taufik.managemoney.data.repo.AccountRepository
import com.taufik.managemoney.domain.ai.ImagePayload
import com.taufik.managemoney.domain.ai.ImageTransactionParser
import com.taufik.managemoney.domain.model.ParsedTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/** Model DeepSeek yang menerima gambar (deepseek-v4-pro hanya teks). */
internal const val DEEPSEEK_VISION_MODEL = "deepseek-flash"

private const val IMAGE_INSTRUCTION =
    "Baca struk/bukti bayar pada gambar di atas. Ekstrak transaksi sesuai schema JSON."

/** Jalur vision yang tersedia, urut prioritas. */
internal enum class VisionRoute { DEEPSEEK, GEMINI }

/**
 * Urutan jalur vision: provider aktif lebih dulu, lalu provider lain yang punya key.
 * DeepSeek cukup untuk semuanya — Gemini hanya cadangan bila key-nya ada.
 */
internal fun visionRoutes(active: AiProvider, hasDeepSeekKey: Boolean, hasGeminiKey: Boolean): List<VisionRoute> =
    buildList {
        if (active == AiProvider.GEMINI && hasGeminiKey) add(VisionRoute.GEMINI)
        if (hasDeepSeekKey) add(VisionRoute.DEEPSEEK)
        if (hasGeminiKey && VisionRoute.GEMINI !in this) add(VisionRoute.GEMINI)
    }

/**
 * Ekstraksi transaksi dari GAMBAR (struk / bukti QRIS) via model vision:
 * DeepSeek (`deepseek-flash`) atau Gemini. Output schema sama dengan parser teks →
 * lewat [ParserValidator] → layar Konfirmasi yang sama.
 */
@Singleton
internal class ImageParser @Inject constructor(
    private val geminiApi: GeminiApi,
    private val openAiApi: OpenAiApi,
    private val configStore: AiConfigStore,
    private val accountRepository: AccountRepository,
) : ImageTransactionParser {
    override suspend fun parse(images: List<ImagePayload>): Result<List<ParsedTransaction>> {
        if (images.isEmpty()) return Result.failure(ParseException("Tidak ada gambar"))

        val cfg = configStore.current()
        val deepSeekKey = configStore.keyFor(AiProvider.DEEPSEEK)
        val geminiKey = configStore.keyFor(AiProvider.GEMINI)
        val routes = visionRoutes(cfg.provider, deepSeekKey.isNotBlank(), geminiKey.isNotBlank())
        if (routes.isEmpty()) {
            return Result.failure(ParseException("Baca gambar butuh API key DeepSeek (atur di Pengaturan AI)."))
        }

        val codes = accountRepository.observeActiveAccounts().first().map { it.kode }
        val today = LocalDate.now(ZoneId.of("Asia/Jakarta"))
        val system = PromptBuilder.build(today, codes)
        Log.d(TAG, "vision routes=$routes images=${images.size} b64len=${images.sumOf { it.base64.length }}")

        var last: Throwable? = null
        for (route in routes) {
            val text = runCatching {
                when (route) {
                    VisionRoute.DEEPSEEK -> {
                        val baseUrl = if (cfg.provider == AiProvider.DEEPSEEK && cfg.baseUrl.isNotBlank()) cfg.baseUrl
                        else AiProvider.DEEPSEEK.defaultBaseUrl
                        callDeepSeek(baseUrl, deepSeekKey, system, images)
                    }
                    VisionRoute.GEMINI -> {
                        val model = if (cfg.provider == AiProvider.GEMINI) cfg.model else AiProvider.GEMINI.defaultModel
                        callGemini(model, geminiKey, system, images)
                    }
                }
            }.onFailure { Log.w(TAG, "vision $route gagal: ${it.message}") }.getOrNull()

            if (text.isNullOrBlank()) {
                last = last ?: ParseException("Balasan AI kosong")
                continue
            }
            val parsed = ParserValidator.parse(text, codes.toSet())
            if (parsed.isSuccess) {
                Log.d(TAG, "vision OK via $route: ${parsed.getOrNull()?.size} transaksi")
                return parsed
            }
            Log.w(TAG, "vision $route validasi gagal: ${parsed.exceptionOrNull()?.message}; raw=${text.take(400)}")
            last = parsed.exceptionOrNull()
        }
        return Result.failure(ParseException(last?.message ?: "Gagal membaca gambar", last))
    }

    private suspend fun callDeepSeek(
        baseUrl: String,
        key: String,
        system: String,
        images: List<ImagePayload>,
    ): String? {
        val userContent = buildJsonArray {
            images.forEach { img ->
                addJsonObject {
                    put("type", "image_url")
                    putJsonObject("image_url") { put("url", "data:${img.mimeType};base64,${img.base64}") }
                }
            }
            addJsonObject {
                put("type", "text")
                put("text", IMAGE_INSTRUCTION)
            }
        }
        val body = VisionChatRequest(
            model = DEEPSEEK_VISION_MODEL,
            messages = listOf(
                VisionMessage("system", JsonPrimitive(system)),
                VisionMessage("user", userContent),
            ),
            responseFormat = ResponseFormat("json_object"),
            thinking = thinkingFor(baseUrl),
        )
        return withContext(Dispatchers.IO) { openAiApi.chatVision(baseUrl, "Bearer $key", body) }
            .choices.firstOrNull()?.message?.content
    }

    private suspend fun callGemini(model: String, key: String, system: String, images: List<ImagePayload>): String? {
        val parts = buildList {
            images.forEach { add(GeminiPart(inlineData = GeminiInlineData(it.mimeType, it.base64))) }
            add(GeminiPart(text = IMAGE_INSTRUCTION))
        }
        val body = GeminiRequest(
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = system))),
            contents = listOf(GeminiContent(role = "user", parts = parts)),
            generationConfig = GenerationConfig(),
        )
        return withContext(Dispatchers.IO) { geminiApi.generate(model, key, body) }
            .candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
    }
}

private const val TAG = "ImageParser"
