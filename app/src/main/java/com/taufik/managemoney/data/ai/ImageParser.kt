package com.taufik.managemoney.data.ai

import com.taufik.managemoney.data.repo.AccountRepository
import com.taufik.managemoney.domain.ai.ImagePayload
import com.taufik.managemoney.domain.ai.ImageTransactionParser
import com.taufik.managemoney.domain.model.ParsedTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ekstraksi transaksi dari GAMBAR (struk / bukti QRIS) via Gemini vision.
 * Output schema sama dengan parser teks → lewat [ParserValidator] → layar Konfirmasi yang sama.
 * Hanya Gemini (vision); bila key Gemini kosong → gagal anggun dengan pesan jelas.
 */
@Singleton
internal class ImageParser @Inject constructor(
    private val geminiApi: GeminiApi,
    private val configStore: AiConfigStore,
    private val accountRepository: AccountRepository,
) : ImageTransactionParser {
    override suspend fun parse(images: List<ImagePayload>): Result<List<ParsedTransaction>> {
        if (images.isEmpty()) return Result.failure(ParseException("Tidak ada gambar"))

        val key = configStore.keyFor(AiProvider.GEMINI)
        if (key.isBlank()) {
            return Result.failure(ParseException("Baca gambar butuh API key Gemini (atur di Pengaturan)."))
        }
        val cfg = configStore.current()
        val model = if (cfg.provider == AiProvider.GEMINI) cfg.model else AiProvider.GEMINI.defaultModel

        val codes = accountRepository.observeActiveAccounts().first().map { it.kode }
        val today = LocalDate.now(ZoneId.of("Asia/Jakarta"))
        val system = PromptBuilder.build(today, codes)

        val parts = buildList {
            images.forEach { add(GeminiPart(inlineData = GeminiInlineData(it.mimeType, it.base64))) }
            add(GeminiPart(text = "Baca struk/bukti bayar pada gambar di atas. Ekstrak transaksi sesuai schema JSON."))
        }
        val body = GeminiRequest(
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = system))),
            contents = listOf(GeminiContent(role = "user", parts = parts)),
            generationConfig = GenerationConfig(),
        )

        val text = try {
            withContext(Dispatchers.IO) { geminiApi.generate(model, key, body) }
                .candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            return Result.failure(ParseException("Gagal membaca gambar via Gemini", e))
        } ?: return Result.failure(ParseException("Balasan Gemini kosong"))

        return ParserValidator.parse(text, codes.toSet())
    }
}
