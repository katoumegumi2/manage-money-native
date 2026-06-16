package com.taufik.managemoney.data.ai

import retrofit2.http.Body
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
}
