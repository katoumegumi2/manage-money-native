package com.taufik.managemoney.domain.ai

import com.taufik.managemoney.domain.model.ParsedTransaction
import java.time.LocalDate

/**
 * Boundary AI: domain & UI HANYA bergantung pada interface ini. Provider (Gemini/OpenAI-compatible)
 * adalah adapter di `data/ai` yang bisa ditukar tanpa menyentuh domain/ui (brief §3.2).
 */
interface TransactionParser {
    suspend fun parse(req: ParseRequest): Result<List<ParsedTransaction>>
}

/** Permintaan parse: teks mentah + konteks (hari ini WIB + kode akun aktif untuk {{DAFTAR_AKUN}}). */
data class ParseRequest(
    val text: String,
    val today: LocalDate,
    val timezone: String = "Asia/Jakarta",
    val accountCodes: List<String>,
)
