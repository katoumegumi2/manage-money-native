package com.taufik.managemoney.ui.model

import com.taufik.managemoney.core.formatGrouped
import com.taufik.managemoney.domain.model.Transaction
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.util.kategoriLabel
import com.taufik.managemoney.ui.util.relativeDateLabel
import java.time.LocalDate

/** Gaya nominal di baris transaksi — menentukan warna & tanda. */
enum class AmountStyle { INCOME, EXPENSE, TRANSFER }

/** Baris transaksi siap-tampil (judul, subjudul, nominal bertanda, ikon kategori). */
data class TxRowUi(
    val id: String,
    val tipe: TxType,
    val kategori: String,
    val title: String,
    val subtitle: String,
    val amountText: String,
    val style: AmountStyle,
)

/**
 * Petakan transaksi → baris tampilan.
 * @param namesById nama akun per id (untuk subjudul).
 * @param today acuan tanggal relatif (WIB).
 * @param perspectiveId bila diisi (layar Detail akun), tanda +/− dihitung dari sudut pandang akun itu;
 *   bila null (Beranda), transfer ditampilkan netral (tanpa tanda).
 */
fun Transaction.toRowUi(
    namesById: Map<String, String>,
    today: LocalDate,
    perspectiveId: String? = null,
): TxRowUi {
    val dari = akunDariId?.let { namesById[it] } ?: "?"
    val ke = akunKeId?.let { namesById[it] } ?: "?"
    val rel = relativeDateLabel(tanggal, today)

    val title = catatan.ifBlank {
        if (tipe == TxType.TRANSFER) "Transfer" else kategoriLabel(kategori)
    }
    val subtitle = when (tipe) {
        TxType.TRANSFER -> "$dari → $ke · $rel"
        TxType.PENGELUARAN -> "${kategoriLabel(kategori)} · $dari · $rel"
        TxType.PEMASUKAN -> "${kategoriLabel(kategori)} · $ke · $rel"
    }
    val style = when (tipe) {
        TxType.PENGELUARAN -> AmountStyle.EXPENSE
        TxType.PEMASUKAN -> AmountStyle.INCOME
        TxType.TRANSFER ->
            when (perspectiveId) {
                akunDariId -> AmountStyle.EXPENSE
                akunKeId -> AmountStyle.INCOME
                else -> AmountStyle.TRANSFER
            }
    }
    val sign = when (style) {
        AmountStyle.INCOME -> "+"
        AmountStyle.EXPENSE -> "−" // minus tipografis
        AmountStyle.TRANSFER -> ""
    }
    return TxRowUi(
        id = id,
        tipe = tipe,
        kategori = kategori,
        title = title,
        subtitle = subtitle,
        amountText = "$sign${formatGrouped(nominal)}",
        style = style,
    )
}
