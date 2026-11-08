package com.taufik.managemoney.ui.voice

/**
 * Aturan sesi suara "menyambung": SpeechRecognizer Google berhenti sendiri setiap jeda ±1–2 detik
 * (extra durasi hening sering diabaikan). Maka tiap segmen selesai kita mulai ulang & gabungkan teksnya,
 * sampai user diam ≥ [SILENCE_FINISH_MS], menekan "Selesai", atau total ≥ [MAX_LISTEN_MS].
 */
internal object VoiceSession {
    /** Diam selama ini (sejak ucapan terakhir) → anggap selesai bicara. */
    const val SILENCE_FINISH_MS = 4_000L

    /** Batas total satu sesi, cegah mic menyala tanpa akhir. */
    const val MAX_LISTEN_MS = 90_000L

    fun shouldContinue(silentMs: Long, totalMs: Long): Boolean =
        silentMs < SILENCE_FINISH_MS && totalMs < MAX_LISTEN_MS

    /** Gabung teks segmen ke teks sebelumnya (dipisah spasi, abaikan segmen kosong). */
    fun join(committed: String, segment: String): String =
        listOf(committed.trim(), segment.trim()).filter { it.isNotEmpty() }.joinToString(" ")

    /** Final kadang lebih pendek/buruk dari partial terakhir → pilih yang lebih lengkap. */
    fun best(finalText: String, lastPartial: String): String =
        if (finalText.length >= lastPartial.length) finalText else lastPartial
}
