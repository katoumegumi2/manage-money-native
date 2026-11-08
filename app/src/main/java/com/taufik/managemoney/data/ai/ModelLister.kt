package com.taufik.managemoney.data.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Model yang tersedia di server provider, untuk dipilih di Pengaturan AI. */
data class RemoteModel(
    val id: String,
    /** Nama tampilan versi sebenarnya bila provider memberi (mis. "DeepSeek-V4.1-Flash"). */
    val name: String?,
    val supportsImage: Boolean,
)

/**
 * Ambil daftar model langsung dari provider (`GET /models`), supaya user selalu melihat model
 * terbaru tanpa menunggu update app. Hanya provider OpenAI-compatible.
 */
@Singleton
class ModelLister @Inject internal constructor(private val api: OpenAiApi) {
    suspend fun list(config: AiConfig): Result<List<RemoteModel>> {
        if (!config.provider.openAiCompatible) return Result.failure(ParseException("Provider ini tak mendukung daftar model"))
        if (config.apiKey.isBlank()) return Result.failure(ParseException("Isi API key dulu"))
        val url = modelsUrlFor(config.baseUrl) ?: return Result.failure(ParseException("Base URL tak dikenali"))
        return runCatching {
            withContext(Dispatchers.IO) { api.models(url, "Bearer ${config.apiKey}") }.data.map {
                RemoteModel(it.id, it.name, it.inputModalities?.contains("image") == true)
            }
        }.recoverCatching { throw ParseException("Gagal memuat daftar model", it) }
    }
}
