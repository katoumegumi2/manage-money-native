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
) {
    GEMINI("Gemini", "", "gemini-2.5-flash-lite", false),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com/chat/completions", "deepseek-chat", true),
    OPENAI("OpenAI", "https://api.openai.com/v1/chat/completions", "gpt-4o-mini", true),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1/chat/completions", "deepseek/deepseek-chat-v3-0324:free", true),
    GROQ("Groq", "https://api.groq.com/openai/v1/chat/completions", "llama-3.3-70b-versatile", true),
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
