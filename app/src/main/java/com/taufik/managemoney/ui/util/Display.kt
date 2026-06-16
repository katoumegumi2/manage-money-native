package com.taufik.managemoney.ui.util

import androidx.compose.ui.graphics.Color
import com.taufik.managemoney.domain.model.AccountType
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.theme.BadgeBni
import com.taufik.managemoney.ui.theme.BadgeGopay
import com.taufik.managemoney.ui.theme.BadgeMandiri
import com.taufik.managemoney.ui.theme.BadgeSeabank
import com.taufik.managemoney.ui.theme.BadgeShopeepay
import com.taufik.managemoney.ui.theme.BadgeTunai
import com.taufik.managemoney.ui.theme.Expense
import com.taufik.managemoney.ui.theme.Income
import com.taufik.managemoney.ui.theme.Transfer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val localeId = Locale.forLanguageTag("id-ID")
private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", localeId)

/** "hari ini" / "kemarin" / "N hari lalu" / "12 Jun 2026". `today` di-inject agar dapat dites. */
fun relativeDateLabel(date: LocalDate, today: LocalDate): String {
    val days = ChronoUnit.DAYS.between(date, today)
    return when {
        days == 0L -> "hari ini"
        days == 1L -> "kemarin"
        days in 2..6 -> "$days hari lalu"
        else -> date.format(dateFormatter)
    }
}

/** Kapitalkan label kategori, mis. "makanan" → "Makanan". */
fun kategoriLabel(kategori: String): String =
    kategori.replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeId) else it.toString() }

/** Label jenis akun untuk tile/detail. */
fun jenisLabel(jenis: AccountType): String = when (jenis) {
    AccountType.BANK -> "Bank"
    AccountType.EWALLET -> "E-Wallet"
    AccountType.TUNAI -> "Tunai"
}

/** Warna aksen tipe transaksi (tag/aksen pada kartu konfirmasi). */
fun txTypeColor(tipe: TxType): Color = when (tipe) {
    TxType.PENGELUARAN -> Expense
    TxType.PEMASUKAN -> Income
    TxType.TRANSFER -> Transfer
}

/** Label tipe transaksi (sentence case) untuk tag/segmen. */
fun txTypeLabel(tipe: TxType): String = when (tipe) {
    TxType.PENGELUARAN -> "Pengeluaran"
    TxType.PEMASUKAN -> "Pemasukan"
    TxType.TRANSFER -> "Transfer"
}

/** Emoji ikon baris transaksi berdasar tipe & kategori. */
fun emojiFor(tipe: TxType, kategori: String): String {
    if (tipe == TxType.TRANSFER) return "↔️" // ↔️
    return when (kategori.lowercase(localeId)) {
        "makanan" -> "🍽️" // 🍽️
        "transport" -> "🛵" // 🛵
        "belanja" -> "🛒" // 🛒
        "tagihan" -> "💡" // 💡
        "hiburan" -> "🎬" // 🎬
        "kesehatan" -> "💊" // 💊
        "admin" -> "🧾" // 🧾
        "gaji" -> "💰" // 💰
        "usaha" -> "💼" // 💼
        "bonus" -> "🎁" // 🎁
        "sampingan" -> "💸" // 💸
        else -> if (tipe == TxType.PEMASUKAN) "💵" else "🧾" // 💵 / 🧾
    }
}

/** Warna + singkatan badge akun. Dipetakan per kode (brand), fallback per jenis. */
data class AccountVisual(val color: Color, val abbr: String)

fun accountVisual(kode: String, jenis: AccountType): AccountVisual {
    val color = when (kode.lowercase(localeId)) {
        "bni" -> BadgeBni
        "mandiri" -> BadgeMandiri
        "seabank" -> BadgeSeabank
        "gopay" -> BadgeGopay
        "shopeepay" -> BadgeShopeepay
        "tunai" -> BadgeTunai
        else -> when (jenis) {
            AccountType.BANK -> BadgeMandiri
            AccountType.EWALLET -> BadgeGopay
            AccountType.TUNAI -> BadgeTunai
        }
    }
    val abbr = when (kode.lowercase(localeId)) {
        "mandiri" -> "MDR"
        "seabank" -> "SEA"
        "gopay" -> "GP"
        "shopeepay" -> "SP"
        "tunai" -> "TN"
        else -> kode.take(3).uppercase(localeId)
    }
    return AccountVisual(color, abbr)
}
