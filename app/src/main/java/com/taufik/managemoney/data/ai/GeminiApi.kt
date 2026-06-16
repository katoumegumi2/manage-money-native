package com.taufik.managemoney.data.ai

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit service Gemini native. Base URL: https://generativelanguage.googleapis.com/
 * Key dikirim via header `x-goog-api-key` (bukan query) agar tidak muncul di URL/log.
 */
internal interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generate(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body body: GeminiRequest,
    ): GeminiResponse
}
