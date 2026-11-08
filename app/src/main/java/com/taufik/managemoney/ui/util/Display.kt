package com.taufik.managemoney.ui.util

import com.taufik.managemoney.R

import androidx.compose.ui.graphics.Color
import com.taufik.managemoney.domain.model.AccountType
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.theme.BadgeBni
import com.taufik.managemoney.ui.theme.BadgeGopay
import com.taufik.managemoney.ui.theme.BadgeMandiri
import com.taufik.managemoney.ui.theme.BadgeSeabank
import com.taufik.managemoney.ui.theme.BadgeShopeepay
import com.taufik.managemoney.ui.theme.BadgeTunai
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
fun kategoriLabel(kategori: String): String = when (kategori) {
    "bayar_utang" -> "Bayar utang"
    "pinjamkan" -> "Beri pinjaman"
    "penyesuaian" -> "Koreksi saldo"
    "bbm" -> "BBM"
    else -> kategori.replace('_', ' ').replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeId) else it.toString() }
}

/** Label jenis akun untuk tile/detail. */
fun jenisLabel(jenis: AccountType): String = when (jenis) {
    AccountType.BANK -> "Bank"
    AccountType.EWALLET -> "E-Wallet"
    AccountType.TUNAI -> "Tunai"
}

/** Warna aksen tipe transaksi (tag/aksen pada kartu konfirmasi). */
@androidx.compose.runtime.Composable
fun txTypeColor(tipe: TxType): Color {
    val c = com.taufik.managemoney.ui.theme.AppTheme.colors
    return when (tipe) {
        TxType.PENGELUARAN -> c.expense
        TxType.PEMASUKAN -> c.income
        TxType.TRANSFER -> c.transfer
    }
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

// ---------------------------------------------------------------------------
// Ikon kategori (Redesign v4): ikon Solar Bold Duotone (res/drawable/sol_cat_*.xml).
// Warna TIDAK disimpan di sini — dirender dari AppTheme (masuk/keluar mengikuti mode warna).
// ---------------------------------------------------------------------------
data class CategoryVisual(@androidx.annotation.DrawableRes val icon: Int)

fun categoryVisual(tipe: TxType, kategori: String): CategoryVisual {
    if (tipe == TxType.TRANSFER) return CategoryVisual(R.drawable.sol_transfer)
    val icon = when (kategori.lowercase(localeId)) {
        "makanan" -> R.drawable.sol_cat_makanan
        "belanja" -> R.drawable.sol_cat_belanja
        "transport" -> R.drawable.sol_cat_transport
        "bbm" -> R.drawable.sol_cat_bbm
        "tagihan" -> R.drawable.sol_cat_tagihan
        "langganan" -> R.drawable.sol_cat_langganan
        "hiburan" -> R.drawable.sol_cat_hiburan
        "kesehatan" -> R.drawable.sol_cat_kesehatan
        "perawatan" -> R.drawable.sol_cat_perawatan
        "laundry" -> R.drawable.sol_cat_laundry
        "admin" -> R.drawable.sol_cat_admin
        "investasi" -> R.drawable.sol_cat_investasi
        "gaji" -> R.drawable.sol_cat_gaji
        "usaha" -> R.drawable.sol_cat_usaha
        "bonus" -> R.drawable.sol_cat_bonus
        "sampingan" -> R.drawable.sol_cat_sampingan
        "penjualan" -> R.drawable.sol_cat_penjualan
        "refund" -> R.drawable.sol_cat_refund
        "pendidikan" -> R.drawable.sol_cat_pendidikan
        "sosial" -> R.drawable.sol_cat_sosial
        "keluarga" -> R.drawable.sol_cat_keluarga
        "perjalanan" -> R.drawable.sol_cat_perjalanan
        "game" -> R.drawable.sol_cat_game
        "pinjamkan" -> R.drawable.sol_cat_pinjamkan
        "bayar_utang" -> R.drawable.sol_cat_bayar_utang
        "reimburse" -> R.drawable.sol_cat_reimburse
        "pengembalian" -> R.drawable.sol_cat_pengembalian
        "pinjaman" -> R.drawable.sol_cat_pinjaman
        "hadiah" -> R.drawable.sol_cat_hadiah
        "cashback" -> R.drawable.sol_cat_cashback
        "penyesuaian" -> R.drawable.sol_cat_penyesuaian
        "transfer" -> R.drawable.sol_transfer
        else -> if (tipe == TxType.PEMASUKAN) R.drawable.sol_cat_pemasukan else R.drawable.sol_cat_lainnya
    }
    return CategoryVisual(icon)
}

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
