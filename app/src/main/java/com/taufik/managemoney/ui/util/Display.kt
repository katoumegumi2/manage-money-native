package com.taufik.managemoney.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

// ---------------------------------------------------------------------------
// Ikon + tint bulat kategori (redesign v3). Baris transaksi memakai ikon garis
// dalam lingkaran ber-tint kategori, menggantikan emoji.
// Tint [bg, stroke] mengikuti docs/private/design_handoff_catatan_keuangan.
// ---------------------------------------------------------------------------
data class CategoryVisual(val bg: Color, val stroke: Color, val icon: ImageVector)

private val incomeTint = Color(0xFFEAFBF1) to Color(0xFF12B76A)
private val neutralTint = Color(0xFFEEF0F3) to Color(0xFF667085)

fun categoryVisual(tipe: TxType, kategori: String): CategoryVisual {
    if (tipe == TxType.TRANSFER) {
        return CategoryVisual(Color(0xFFEEF0F3), Color(0xFF475467), Icons.Filled.SwapHoriz)
    }
    return when (kategori.lowercase(localeId)) {
        // Pengeluaran
        "makanan" -> CategoryVisual(Color(0xFFFFF4E5), Color(0xFFF2843B), Icons.Filled.Restaurant)
        "belanja" -> CategoryVisual(Color(0xFFFCEAF3), Color(0xFFD9569E), Icons.Filled.ShoppingBag)
        "transport" -> CategoryVisual(Color(0xFFE7F7F2), Color(0xFF12B0A0), Icons.Filled.DirectionsBus)
        "bbm" -> CategoryVisual(Color(0xFFFDEFE7), Color(0xFFE8833A), Icons.Filled.LocalGasStation)
        "tagihan" -> CategoryVisual(Color(0xFFE8F1FD), Color(0xFF2E7DD8), Icons.Filled.ReceiptLong)
        "langganan" -> CategoryVisual(Color(0xFFF1ECFB), Color(0xFF8B5CF6), Icons.Filled.Autorenew)
        "hiburan" -> CategoryVisual(Color(0xFFF1ECFB), Color(0xFF8B5CF6), Icons.Filled.Movie)
        "kesehatan" -> CategoryVisual(Color(0xFFFDECEA), Color(0xFFF04438), Icons.Filled.MedicalServices)
        "perawatan" -> CategoryVisual(Color(0xFFFCEAF3), Color(0xFFD9569E), Icons.Filled.Spa)
        "laundry" -> CategoryVisual(Color(0xFFE7F7F2), Color(0xFF12B0A0), Icons.Filled.LocalLaundryService)
        "admin" -> CategoryVisual(Color(0xFFEEF0F3), Color(0xFF667085), Icons.Filled.Receipt)
        "investasi" -> CategoryVisual(incomeTint.first, incomeTint.second, Icons.Filled.TrendingUp)
        // Pemasukan
        "gaji" -> CategoryVisual(incomeTint.first, incomeTint.second, Icons.Filled.Payments)
        "usaha" -> CategoryVisual(incomeTint.first, incomeTint.second, Icons.Filled.BusinessCenter)
        "bonus" -> CategoryVisual(incomeTint.first, incomeTint.second, Icons.Filled.CardGiftcard)
        "sampingan" -> CategoryVisual(incomeTint.first, incomeTint.second, Icons.Filled.Bolt)
        "penjualan" -> CategoryVisual(incomeTint.first, incomeTint.second, Icons.Filled.Sell)
        "refund" -> CategoryVisual(incomeTint.first, incomeTint.second, Icons.Filled.Replay)
        else -> if (tipe == TxType.PEMASUKAN) {
            CategoryVisual(incomeTint.first, incomeTint.second, Icons.Filled.Payments)
        } else {
            CategoryVisual(neutralTint.first, neutralTint.second, Icons.Filled.MoreHoriz)
        }
    }
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
