package com.taufik.managemoney.data.ai

import com.taufik.managemoney.domain.ai.ParseRequest
import com.taufik.managemoney.domain.ai.TransactionParser
import com.taufik.managemoney.domain.model.ParsedTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Adapter untuk semua provider format OpenAI Chat Completions (DeepSeek/OpenAI/OpenRouter/Groq/Custom).
 * Prompt builder + validator SAMA seperti Gemini; beda hanya baseUrl + model + apiKey.
 * Gagal HTTP/JSON → [Result.failure] (UI fallback manual), tak pernah crash.
 */
internal class OpenAiCompatibleParser(
    private val api: OpenAiApi,
    private val baseUrl: String,
    private val apiKey: String,
    private val model: String,
) : TransactionParser {

    override suspend fun parse(req: ParseRequest): Result<List<ParsedTransaction>> {
        if (apiKey.isBlank()) return Result.failure(ParseException("API key belum diatur"))
        if (baseUrl.isBlank()) return Result.failure(ParseException("Base URL belum diatur"))

        val prompt = PromptBuilder.build(req.today, req.accountCodes)
        val body = ChatRequest(
            model = model,
            messages = listOf(
                ChatMessage(role = "system", content = prompt),
                ChatMessage(role = "user", content = req.text),
            ),
            responseFormat = ResponseFormat("json_object"),
            thinking = thinkingFor(baseUrl),
        )

        val response = try {
            withContext(Dispatchers.IO) { api.chat(baseUrl, "Bearer $apiKey", body) }
        } catch (e: Exception) {
            return Result.failure(ParseException("Gagal menghubungi server AI", e))
        }

        val text = response.choices.firstOrNull()?.message?.content
            ?: return Result.failure(ParseException("Balasan server AI kosong"))

        return ParserValidator.parse(text, req.accountCodes.toSet())
    }
}
