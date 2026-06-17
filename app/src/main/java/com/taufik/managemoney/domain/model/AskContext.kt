package com.taufik.managemoney.domain.model

/** Akun + saldo untuk konteks Tanya AI. */
data class AskAccount(val nama: String, val saldo: Long)

/**
 * Konteks agregat untuk "Tanya AI" — DIHITUNG app, dikirim ke LLM sebagai DATA.
 * Hanya angka agregat (bukan transaksi mentah) demi privasi & hemat.
 */
data class AskContext(
    val today: String,        // YYYY-MM-DD (resolve "bulan ini", "hari ini")
    val period: String,       // "Juni 2026"
    val masuk: Long,
    val keluar: Long,
    val net: Long,
    val prevMasuk: Long,
    val prevKeluar: Long,
    val categories: List<CategoryAgg>, // pengeluaran per kategori bulan ini
    val accounts: List<AskAccount>,    // saldo akun aktif
)
