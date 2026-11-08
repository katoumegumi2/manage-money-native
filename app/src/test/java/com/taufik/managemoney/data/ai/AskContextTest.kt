package com.taufik.managemoney.data.ai

import com.taufik.managemoney.domain.ai.ChatTurn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Riwayat chat Tanya AI, migrasi model DeepSeek lama, dan saklar thinking DeepSeek. */
class AskContextTest {

    @Test
    fun `riwayat jadi pesan OpenAI berurutan dengan role benar`() {
        val history = listOf(
            ChatTurn(true, "pengeluaran bagaimana?"),
            ChatTurn(false, "Pengeluaran bulan ini Rp 961.000"),
        )
        val msgs = history.toOpenAiMessages()
        assertEquals(listOf("user", "assistant"), msgs.map { it.role })
        assertEquals("pengeluaran bagaimana?", msgs[0].content)
    }

    @Test
    fun `riwayat jadi konten Gemini dengan role model`() {
        val contents = listOf(ChatTurn(true, "a"), ChatTurn(false, "b")).toGeminiContents()
        assertEquals(listOf("user", "model"), contents.map { it.role })
    }

    @Test
    fun `riwayat dipotong ke pesan terakhir dan pesan kosong dibuang`() {
        val history = (1..50).map { ChatTurn(it % 2 == 1, "pesan $it") } + ChatTurn(false, "  ")
        val recent = history.recent()
        assertEquals(MAX_HISTORY_MESSAGES, recent.size)
        assertEquals("pesan 11", recent.first().text)
        assertEquals("pesan 50", recent.last().text)
    }

    @Test
    fun `model DeepSeek lama dimigrasi ke deepseek-flash`() {
        listOf("deepseek-chat", "deepseek-reasoner", "deepseek-v4-flash", " DeepSeek-Chat ").forEach {
            assertEquals("deepseek-flash", AiProvider.DEEPSEEK.migrateModel(it))
        }
        assertEquals("deepseek-v4-pro", AiProvider.DEEPSEEK.migrateModel("deepseek-v4-pro"))
        // provider lain tak tersentuh, meski nama modelnya mirip
        assertEquals("deepseek/deepseek-chat-v3-0324:free", AiProvider.OPENROUTER.migrateModel("deepseek/deepseek-chat-v3-0324:free"))
    }

    @Test
    fun `thinking dimatikan hanya untuk endpoint DeepSeek`() {
        assertEquals(Thinking("disabled"), thinkingFor("https://api.deepseek.com/chat/completions"))
        assertNull(thinkingFor("https://api.openai.com/v1/chat/completions"))
        assertNull(thinkingFor("https://openrouter.ai/api/v1/chat/completions"))
    }
}

class VisionAndModelsTest {
    @Test
    fun `url models diturunkan dari url chat completions`() {
        assertEquals("https://api.deepseek.com/models", modelsUrlFor("https://api.deepseek.com/chat/completions"))
        assertEquals("https://api.openai.com/v1/models", modelsUrlFor("https://api.openai.com/v1/chat/completions/"))
        assertNull(modelsUrlFor("https://contoh.local/api/generate"))
    }

    @Test
    fun `vision pakai DeepSeek tanpa butuh Gemini`() {
        assertEquals(listOf(VisionRoute.DEEPSEEK), visionRoutes(AiProvider.DEEPSEEK, hasDeepSeekKey = true, hasGeminiKey = false))
        // Gemini hanya cadangan bila key-nya ada
        assertEquals(
            listOf(VisionRoute.DEEPSEEK, VisionRoute.GEMINI),
            visionRoutes(AiProvider.DEEPSEEK, hasDeepSeekKey = true, hasGeminiKey = true),
        )
        assertEquals(
            listOf(VisionRoute.GEMINI, VisionRoute.DEEPSEEK),
            visionRoutes(AiProvider.GEMINI, hasDeepSeekKey = true, hasGeminiKey = true),
        )
        assertEquals(emptyList<VisionRoute>(), visionRoutes(AiProvider.OPENAI, hasDeepSeekKey = false, hasGeminiKey = false))
    }
}
