package com.taufik.managemoney.data.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTO endpoint native Gemini `generateContent` (lihat brief §3.3). Hanya dipakai di paket data/ai. */
@Serializable
internal data class GeminiRequest(
    @SerialName("system_instruction") val systemInstruction: GeminiContent,
    val contents: List<GeminiContent>,
    @SerialName("generationConfig") val generationConfig: GenerationConfig,
)

@Serializable
internal data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null,
)

@Serializable
internal data class GeminiPart(val text: String)

@Serializable
internal data class GenerationConfig(
    @SerialName("responseMimeType") val responseMimeType: String = "application/json",
    val temperature: Double = 0.0,
)

@Serializable
internal data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
)

@Serializable
internal data class GeminiCandidate(
    val content: GeminiContent? = null,
    @SerialName("finishReason") val finishReason: String? = null,
)
