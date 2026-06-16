package com.taufik.managemoney.data.ai

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.taufik.managemoney.domain.ai.TransactionParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * DI parser AI. Berada di `data/ai` agar nama provider (Gemini) tidak bocor ke luar paket ini.
 * Ganti provider = ganti binding `TransactionParser` di sini (brief §3.4).
 */
@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(GEMINI_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    internal fun provideGeminiApi(retrofit: Retrofit): GeminiApi = retrofit.create(GeminiApi::class.java)

    // OpenAI-compatible: pakai @Url absolut, jadi baseUrl Retrofit (Gemini) diabaikan saat dipanggil.
    @Provides
    @Singleton
    internal fun provideOpenAiApi(retrofit: Retrofit): OpenAiApi = retrofit.create(OpenAiApi::class.java)

    // Parser aktif = ConfigurableParser (pilih provider dari AiConfig saat runtime).
    @Provides
    @Singleton
    internal fun provideTransactionParser(parser: ConfigurableParser): TransactionParser = parser
}
