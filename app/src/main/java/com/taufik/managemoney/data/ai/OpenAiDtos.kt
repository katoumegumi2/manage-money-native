package com.taufik.managemoney.data.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
)

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
