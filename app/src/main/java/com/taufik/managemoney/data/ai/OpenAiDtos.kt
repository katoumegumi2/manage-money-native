package com.taufik.managemoney.data.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTO OpenAI Chat Completions (dipakai semua provider OpenAI-compatible). Hanya di paket data/ai. */
@Serializable
internal data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.0,
    @SerialName("response_format") val responseFormat: ResponseFormat? = null,
)

@Serializable
internal data class ChatMessage(
    val role: String,
    val content: String,
)

@Serializable
internal data class ResponseFormat(val type: String)

@Serializable
internal data class ChatResponse(
    val choices: List<ChatChoice> = emptyList(),
)

@Serializable
internal data class ChatChoice(
    val message: ChatMessage? = null,
)
