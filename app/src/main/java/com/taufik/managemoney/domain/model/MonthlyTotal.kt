package com.taufik.managemoney.domain.model

/** Total nominal satu tipe (pemasukan/pengeluaran) untuk satu bulan "YYYY-MM". */
data class MonthlyTotal(val ym: String, val tipe: TxType, val total: Long)
