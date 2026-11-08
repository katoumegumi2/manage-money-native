package com.taufik.managemoney.data.ai

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Retrofit service format OpenAI Chat Completions. URL penuh dikirim per panggilan (@Url)
 * agar baseUrl provider bisa berubah saat runtime tanpa rebuild. Key via header Authorization Bearer.
 */
internal interface OpenAiApi {
    @POST
    suspend fun chat(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body body: ChatRequest,
    ): ChatResponse

    /** Chat dengan konten multimodal (teks + gambar) — dipakai baca struk via DeepSeek. */
    @POST
    suspend fun chatVision(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body body: VisionChatRequest,
    ): ChatResponse

    /** Daftar model yang tersedia di server provider (`GET …/models`). */
    @GET
    suspend fun models(
        @Url url: String,
        @Header("Authorization") authorization: String,
    ): ModelListResponse
}
