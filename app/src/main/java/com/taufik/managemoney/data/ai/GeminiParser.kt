package com.taufik.managemoney.data.ai

import com.taufik.managemoney.domain.ai.ParseRequest
import com.taufik.managemoney.domain.ai.TransactionParser
import com.taufik.managemoney.domain.model.ParsedTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Adapter Gemini (endpoint native). Pakai prompt builder + validator yang sama untuk semua provider.
 * Hasil HTTP/JSON gagal → [Result.failure] (UI fallback manual), tidak pernah crash.
 */
internal class GeminiParser(
    private val api: GeminiApi,
    private val apiKey: String,
    private val model: String = "gemini-2.5-flash-lite",
) : TransactionParser {

    override suspend fun parse(req: ParseRequest): Result<List<ParsedTransaction>> {
        if (apiKey.isBlank()) {
            return Result.failure(ParseException("API key Gemini belum diatur"))
        }
        val prompt = PromptBuilder.build(req.today, req.accountCodes)
        val body = GeminiRequest(
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(prompt))),
            contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(req.text)))),
            generationConfig = GenerationConfig(),
        )

        val response = try {
            withContext(Dispatchers.IO) { api.generate(model, apiKey, body) }
        } catch (e: Exception) {
            return Result.failure(ParseException("Gagal menghubungi Gemini", e))
        }

        val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: return Result.failure(ParseException("Balasan Gemini kosong"))

        return ParserValidator.parse(text, req.accountCodes.toSet())
    }
}
