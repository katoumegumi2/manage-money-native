package com.taufik.managemoney.data.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** DTO OpenAI Chat Completions (dipakai semua provider OpenAI-compatible). Hanya di paket data/ai. */
@Serializable
internal data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.0,
    @SerialName("response_format") val responseFormat: ResponseFormat? = null,
    /** Hanya diisi untuk Tanya AI (function calling); null → tak dikirim. */
    val tools: List<OpenAiTool>? = null,
    @SerialName("tool_choice") val toolChoice: String? = null,
    /** Khusus DeepSeek (V4): saklar mode thinking. null → tak dikirim (provider lain menolak field asing). */
    val thinking: Thinking? = null,
)

@Serializable
internal data class Thinking(val type: String)

/**
 * DeepSeek V4 menyalakan thinking secara default. Kita matikan: (1) jawaban lebih cepat & murah,
 * (2) thinking + tools mewajibkan `reasoning_content` dikirim balik tiap giliran (kalau tidak → HTTP 400).
 * Dideteksi dari baseUrl agar preset Custom yang menunjuk ke DeepSeek ikut tertangani.
 */
internal fun thinkingFor(baseUrl: String): Thinking? =
    if (baseUrl.contains("api.deepseek.com", ignoreCase = true)) Thinking("disabled") else null

@Serializable
internal data class ChatMessage(
    val role: String,
    val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<OpenAiToolCall>? = null,
    @SerialName("tool_call_id") val toolCallId: String? = null,
    val name: String? = null,
)

@Serializable
internal data class ResponseFormat(val type: String)

/** Deklarasi tool untuk function calling OpenAI-compatible. */
@Serializable
internal data class OpenAiTool(
    val type: String = "function",
    val function: OpenAiFunctionDef,
)

@Serializable
internal data class OpenAiFunctionDef(
    val name: String,
    val description: String,
    /** JSON Schema object parameter. */
    val parameters: JsonObject,
)

@Serializable
internal data class OpenAiToolCall(
    val id: String = "",
    val type: String = "function",
    val function: OpenAiFunctionCall,
)

/** `arguments` adalah STRING berisi JSON (kontrak OpenAI), bukan objek. */
@Serializable
internal data class OpenAiFunctionCall(
    val name: String,
    val arguments: String = "",
)

@Serializable
internal data class ChatResponse(
    val choices: List<ChatChoice> = emptyList(),
)

@Serializable
internal data class ChatChoice(
    val message: ChatMessage? = null,
)

/** Request chat multimodal: `content` bisa string (system) atau array part teks/gambar (user). */
@Serializable
internal data class VisionChatRequest(
    val model: String,
    val messages: List<VisionMessage>,
    val temperature: Double = 0.0,
    @SerialName("response_format") val responseFormat: ResponseFormat? = null,
    val thinking: Thinking? = null,
)

@Serializable
internal data class VisionMessage(val role: String, val content: JsonElement)

/** Respons `GET /models`. Field selain `id` opsional (tiap provider beda kelengkapan). */
@Serializable
internal data class ModelListResponse(val data: List<RemoteModelDto> = emptyList())

@Serializable
internal data class RemoteModelDto(
    val id: String,
    val name: String? = null,
    @SerialName("input_modalities") val inputModalities: List<String>? = null,
    @SerialName("context_window") val contextWindow: Long? = null,
)

/**
 * URL daftar model dari URL chat completions: `…/chat/completions` → `…/models`.
 * null bila format URL tak dikenali (mis. Custom yang tak standar).
 */
internal fun modelsUrlFor(chatUrl: String): String? {
    val url = chatUrl.trim().trimEnd('/')
    return if (url.endsWith("/chat/completions")) url.removeSuffix("/chat/completions") + "/models" else null
}
