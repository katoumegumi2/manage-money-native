package com.taufik.managemoney.data.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Prompt builder §3.6 — isi placeholder {{HARI_INI}} & {{DAFTAR_AKUN}}. */
class PromptBuilderTest {

    @Test
    fun `placeholder hari ini dan daftar akun terisi`() {
        val prompt = PromptBuilder.build(
            today = LocalDate.of(2026, 6, 16),
            accountCodes = listOf("bni", "mandiri", "tunai"),
        )
        assertTrue(prompt.contains("2026-06-16"))
        assertTrue(prompt.contains("bni, mandiri, tunai"))
    }

    @Test
    fun `tidak ada placeholder tersisa`() {
        val prompt = PromptBuilder.build(LocalDate.of(2026, 6, 16), listOf("bni"))
        assertFalse(prompt.contains("{{HARI_INI}}"))
        assertFalse(prompt.contains("{{DAFTAR_AKUN}}"))
    }
}
