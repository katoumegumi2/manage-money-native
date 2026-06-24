package com.taufik.managemoney.data.ai

/**
 * Preset provider AI. Semua kecuali Gemini memakai format OpenAI Chat Completions
 * (`OpenAiCompatibleParser`) — beda hanya baseUrl + model + apiKey (brief §3.4).
 * Enum ini sengaja berada di `data/ai` agar nama provider tak bocor ke domain/ui umum;
 * hanya layar Pengaturan AI yang menyebutnya.
 */
enum class AiProvider(
    val displayName: String,
    /** URL lengkap chat completions (untuk OpenAI-compatible). Kosong untuk Gemini (native, URL tetap di kode). */
    val defaultBaseUrl: String,
    val defaultModel: String,
    val openAiCompatible: Boolean,
    /**
     * Saran model untuk pilihan cepat (chip) di Pengaturan AI. Yang lebih mampu = insight lebih detail.
     * Bukan daftar tertutup — field Model tetap bebas diketik bila provider punya model lain.
     */
    val suggestedModels: List<String> = emptyList(),
) {
    GEMINI(
        "Gemini", "", "gemini-2.5-flash-lite", false,
        suggestedModels = listOf("gemini-2.5-flash-lite", "gemini-2.5-flash", "gemini-2.5-pro"),
    ),
    DEEPSEEK(
        "DeepSeek", "https://api.deepseek.com/chat/completions", "deepseek-chat", true,
        suggestedModels = listOf("deepseek-chat", "deepseek-reasoner"),
    ),
    OPENAI(
        "OpenAI", "https://api.openai.com/v1/chat/completions", "gpt-4o-mini", true,
        suggestedModels = listOf("gpt-4o-mini", "gpt-4o", "gpt-4.1-mini", "gpt-4.1"),
    ),
    OPENROUTER(
        "OpenRouter", "https://openrouter.ai/api/v1/chat/completions", "deepseek/deepseek-chat-v3-0324:free", true,
        suggestedModels = listOf("deepseek/deepseek-chat-v3-0324:free", "deepseek/deepseek-r1:free", "google/gemini-2.5-flash"),
    ),
    GROQ(
        "Groq", "https://api.groq.com/openai/v1/chat/completions", "llama-3.3-70b-versatile", true,
        suggestedModels = listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant"),
    ),
    CUSTOM("Custom (OpenAI-compatible)", "", "", true),
}

/** Konfigurasi parser aktif. `apiKey` disimpan terenkripsi (lihat [AiConfigStore]). */
data class AiConfig(
    val provider: AiProvider,
    val model: String,
    val baseUrl: String,
    val apiKey: String,
) {
    companion object {
        val DEFAULT = AiConfig(
            provider = AiProvider.GEMINI,
            model = AiProvider.GEMINI.defaultModel,
            baseUrl = AiProvider.GEMINI.defaultBaseUrl,
            apiKey = "",
        )
    }
}
