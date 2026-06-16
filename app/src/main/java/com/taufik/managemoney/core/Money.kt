package com.taufik.managemoney.core

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val rupiahSymbols = DecimalFormatSymbols(Locale.forLanguageTag("id-ID")).apply {
    groupingSeparator = '.'
}
private val rupiahFormat = DecimalFormat("#,##0", rupiahSymbols)

/**
 * Format nominal rupiah (Long) menjadi "Rp 1.234.567".
 * Uang selalu integer rupiah penuh — tanpa desimal, tanpa floating point.
 */
fun formatRupiah(amount: Long): String {
    val sign = if (amount < 0) "-" else ""
    return "${sign}Rp ${rupiahFormat.format(kotlin.math.abs(amount))}"
}

/**
 * Format nominal bergrup tanpa awalan "Rp" — mis. "1.234.567".
 * Dipakai di baris transaksi yang sudah memberi tanda (+/−) & warna sendiri.
 */
fun formatGrouped(amount: Long): String = rupiahFormat.format(kotlin.math.abs(amount))
