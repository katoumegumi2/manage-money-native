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
        "bbm" -> "⛽" // ⛽
        "belanja" -> "🛒" // 🛒
        "tagihan" -> "💡" // 💡
        "langganan" -> "🔁" // 🔁
        "hiburan" -> "🎬" // 🎬
        "kesehatan" -> "💊" // 💊
        "perawatan" -> "🔧" // 🔧
        "laundry" -> "🧺" // 🧺
        "admin" -> "🧾" // 🧾
        "investasi" -> "📈" // 📈
        "gaji" -> "💰" // 💰
        "usaha" -> "💼" // 💼
        "bonus" -> "🎁" // 🎁
        "sampingan" -> "💸" // 💸
        "penjualan" -> "🏷️" // 🏷️
        "refund" -> "↩️" // ↩️
        else -> if (tipe == TxType.PEMASUKAN) "💵" else "🧾" // 💵 / 🧾
    }
}

/** Warna + singkatan badge akun. Dipetakan per kode (brand), fallback per jenis. */
data class AccountVisual(val color: Color, val abbr: String)

/** Warna khas brand (perkiraan warna korporat). Dipakai badge fallback bila logo asli belum ada. */
private val brandColor: Map<String, Color> = mapOf(
    // Bank
    "bca" to Color(0xFF1F4E9B),
    "bni" to BadgeBni,
    "bri" to Color(0xFF00529C),
    "mandiri" to BadgeMandiri,
    "bsi" to Color(0xFF00A39D),
    "cimb" to Color(0xFF8C1D2C),
    "permata" to Color(0xFF12877F),
    "danamon" to Color(0xFFF58220),
    "btn" to Color(0xFF00529B),
    "seabank" to BadgeSeabank,
    "jago" to Color(0xFFFF6A00),
    "jenius" to Color(0xFF00B5CC),
    "blu" to Color(0xFF0098DA),
    "allobank" to Color(0xFF1D2A6D),
    "bankjatim" to Color(0xFFC8102E),
    // E-wallet
    "gopay" to Color(0xFF00AED6),
    "ovo" to Color(0xFF4B2E83),
    "dana" to Color(0xFF1391E9),
    "shopeepay" to BadgeShopeepay,
    "linkaja" to Color(0xFFE2211C),
    "sakuku" to Color(0xFF00A0E3),
    "isaku" to Color(0xFFE60012),
    "astrapay" to Color(0xFF11468F),
    // Tunai
    "tunai" to BadgeTunai,
)

/** Singkatan badge (maks ~4 huruf) bila berbeda dari 3 huruf pertama kode. */
private val brandAbbr: Map<String, String> = mapOf(
    "mandiri" to "MDR",
    "seabank" to "SEA",
    "permata" to "PMT",
    "danamon" to "DNM",
    "allobank" to "ALLO",
    "bankjatim" to "JTM",
    "jenius" to "JNS",
    "gopay" to "GP",
    "shopeepay" to "SP",
    "linkaja" to "LINK",
    "sakuku" to "SKK",
    "isaku" to "ISKU",
    "astrapay" to "APAY",
    "tunai" to "TN",
)

fun accountVisual(kode: String, jenis: AccountType): AccountVisual {
    val key = kode.lowercase(localeId)
    val color = brandColor[key] ?: when (jenis) {
        AccountType.BANK -> BadgeMandiri
        AccountType.EWALLET -> BadgeGopay
        AccountType.TUNAI -> BadgeTunai
    }
    val abbr = brandAbbr[key] ?: kode.take(3).uppercase(localeId)
    return AccountVisual(color, abbr)
}
