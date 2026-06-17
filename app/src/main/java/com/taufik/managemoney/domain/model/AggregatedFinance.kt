package com.taufik.managemoney.domain.model

/** Agregat lokal satu periode (bulan) — DIHITUNG app dari ledger, lalu dinarasikan AI. */
data class CategoryAgg(val kategori: String, val total: Long)

data class AggregatedFinance(
    val period: String,       // label tampil, mis. "Juni 2026"
    val periodKey: String,    // "2026-06"
    val masuk: Long,
    val keluar: Long,
    val net: Long,            // masuk - keluar
    val categories: List<CategoryAgg>, // pengeluaran per kategori (desc)
    val prevMasuk: Long,      // bulan sebelumnya (untuk delta)
    val prevKeluar: Long,
) {
    val isEmpty: Boolean get() = masuk == 0L && keluar == 0L && categories.isEmpty()

    /** Tanda tangan ringkas untuk cache: insight di-regenerate hanya bila angka berubah. */
    val signature: String
        get() = buildString {
            append(periodKey); append('|'); append(masuk); append('|'); append(keluar)
            append('|'); append(prevMasuk); append('|'); append(prevKeluar); append('|')
            categories.forEach { append(it.kategori).append(':').append(it.total).append(',') }
        }
}
