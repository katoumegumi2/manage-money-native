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

/**
 * Format ringkas untuk label grafik/ubin: "9,3 jt", "513 rb", "800". Bilangan bulat saja (tanpa floating point):
 * juta dibulatkan ke 1 desimal, ribu dibulatkan ke ribuan terdekat. Tanda minus dipertahankan.
 */
fun formatShort(amount: Long): String {
    val sign = if (amount < 0) "−" else ""
    val a = kotlin.math.abs(amount)
    return sign + when {
        a >= 1_000_000L -> {
            val tenths = (a + 50_000L) / 100_000L // juta ×10, dibulatkan
            val whole = tenths / 10
            val dec = tenths % 10
            if (dec == 0L) "$whole jt" else "$whole,$dec jt"
        }
        a >= 1_000L -> "${(a + 500L) / 1_000L} rb"
        else -> a.toString()
    }
}
