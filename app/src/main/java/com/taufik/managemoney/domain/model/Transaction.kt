package com.taufik.managemoney.domain.model

import java.time.LocalDate

/**
 * Transaksi pada ledger. Menyimpan FK id akun (bukan kode string).
 * - pengeluaran → akunDariId terisi, akunKeId null
 * - pemasukan   → akunKeId terisi, akunDariId null
 * - transfer    → keduanya terisi & berbeda
 */
data class Transaction(
    val id: String = "",
    val tipe: TxType,
    val nominal: Long,
    val kategori: String,
    val akunDariId: String?,
    val akunKeId: String?,
    val tanggal: LocalDate,
    val catatan: String = "",
    val batchId: String? = null,
    val sumberInput: SourceInput,
    val createdAt: Long = System.currentTimeMillis(),
)
