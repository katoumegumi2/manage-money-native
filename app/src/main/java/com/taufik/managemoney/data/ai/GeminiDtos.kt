package com.taufik.managemoney.data.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** DTO endpoint native Gemini `generateContent` (lihat brief §3.3). Hanya dipakai di paket data/ai. */
@Serializable
internal data class GeminiRequest(
    @SerialName("system_instruction") val systemInstruction: GeminiContent,
    val contents: List<GeminiContent>,
    @SerialName("generationConfig") val generationConfig: GenerationConfig,
    /** Hanya diisi untuk Tanya AI (function calling); null → tak dikirim (parser/vision). */
    val tools: List<GeminiTool>? = null,
)

/** Deklarasi tool untuk function calling Gemini. */
@Serializable
internal data class GeminiTool(
    @SerialName("functionDeclarations") val functionDeclarations: List<GeminiFunctionDeclaration>,
)

@Serializable
internal data class GeminiFunctionDeclaration(
    val name: String,
    val description: String,
    /** JSON Schema object parameter. */
    val parameters: JsonObject,
)

/** Bagian functionCall (dari model) / functionResponse (balasan app) untuk loop tool calling. */
@Serializable
internal data class GeminiFunctionCall(
    val name: String,
    val args: JsonObject = JsonObject(emptyMap()),
)

@Serializable
internal data class GeminiFunctionResponse(
    val name: String,
    val response: JsonObject,
)

@Serializable
internal data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null,
)

@Serializable
internal data class GeminiPart(
    val text: String? = null,
    @SerialName("inline_data") val inlineData: GeminiInlineData? = null,
    @SerialName("functionCall") val functionCall: GeminiFunctionCall? = null,
    @SerialName("functionResponse") val functionResponse: GeminiFunctionResponse? = null,
)

/** Bagian gambar untuk input vision Gemini (data = base64 tanpa prefix). */
@Serializable
internal data class GeminiInlineData(
    @SerialName("mime_type") val mimeType: String,
    val data: String,
)

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
