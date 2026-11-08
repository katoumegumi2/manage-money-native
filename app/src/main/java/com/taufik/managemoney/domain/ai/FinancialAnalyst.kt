package com.taufik.managemoney.domain.ai

import com.taufik.managemoney.domain.model.AggregatedFinance

/** Satu giliran percakapan Tanya AI (riwayat untuk konteks pertanyaan lanjutan). */
data class ChatTurn(val fromUser: Boolean, val text: String)

/**
 * Analis keuangan AI: ubah agregat lokal (angka) jadi insight naratif Bahasa Indonesia.
 * App yang menghitung; AI hanya menafsirkan — tidak pernah mengarang angka di luar [data].
 * Provider = adapter yang sama seperti parser (Gemini / OpenAI-compatible), bisa ditukar.
 */
interface FinancialAnalyst {
    suspend fun summarize(data: AggregatedFinance): Result<String>

    /**
     * Tanya-jawab bebas. AI mengambil data sendiri via tools (saldo, ringkasan, cari transaksi)
     * sesuai pertanyaan — tidak ada data dikirim bila tak perlu. Tidak mengarang angka.
     * [history] = giliran sebelumnya (urut lama → baru, TANPA [question]) agar pertanyaan
     * lanjutan seperti "tiap bulannya" dipahami konteksnya.
     */
    suspend fun ask(question: String, history: List<ChatTurn> = emptyList()): Result<String>
}
