package com.taufik.managemoney.domain.ai

import com.taufik.managemoney.domain.model.AggregatedFinance

/**
 * Analis keuangan AI: ubah agregat lokal (angka) jadi insight naratif Bahasa Indonesia.
 * App yang menghitung; AI hanya menafsirkan — tidak pernah mengarang angka di luar [data].
 * Provider = adapter yang sama seperti parser (Gemini / OpenAI-compatible), bisa ditukar.
 */
interface FinancialAnalyst {
    suspend fun summarize(data: AggregatedFinance): Result<String>
}
