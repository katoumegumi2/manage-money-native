package com.taufik.managemoney.ui.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceSessionTest {
    @Test
    fun `jeda pendek lanjut mendengarkan, diam lama atau kelamaan berhenti`() {
        assertTrue(VoiceSession.shouldContinue(silentMs = 1_500, totalMs = 10_000))
        assertFalse(VoiceSession.shouldContinue(silentMs = VoiceSession.SILENCE_FINISH_MS, totalMs = 10_000))
        assertFalse(VoiceSession.shouldContinue(silentMs = 500, totalMs = VoiceSession.MAX_LISTEN_MS))
    }

    @Test
    fun `segmen digabung dengan spasi dan segmen kosong diabaikan`() {
        var text = VoiceSession.join("", "beli kopi 25 ribu")
        text = VoiceSession.join(text, "  ")
        text = VoiceSession.join(text, "sama bensin ceban dari gopay")
        assertEquals("beli kopi 25 ribu sama bensin ceban dari gopay", text)
    }

    @Test
    fun `pilih final atau partial yang lebih lengkap`() {
        assertEquals("beli kopi 25 ribu", VoiceSession.best("beli kopi", "beli kopi 25 ribu"))
        assertEquals("beli kopi 25 ribu", VoiceSession.best("beli kopi 25 ribu", "beli kopi"))
    }
}
