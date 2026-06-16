package com.taufik.managemoney.ui.model

import com.taufik.managemoney.domain.model.DraftIssue
import com.taufik.managemoney.domain.model.ParsedTransaction
import com.taufik.managemoney.domain.model.TransactionDraft
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.domain.model.issues
import java.time.LocalDate

/**
 * Satu transaksi yang sedang diedit di layar (Manual entry / Konfirmasi).
 * Akun sudah berupa FK id (atau null bila belum dipilih). Validasi memakai aturan domain §4.3.
 */
data class EditableTx(
    val tipe: TxType,
    val nominal: Long,
    val kategori: String,
    val akunDariId: Long?,
    val akunKeId: Long?,
    val tanggal: LocalDate,
    val catatan: String = "",
    val perluDicek: Boolean = false,
) {
    fun toDraft(): TransactionDraft =
        TransactionDraft(tipe, nominal, kategori, akunDariId, akunKeId, tanggal, catatan)

    val issues: Set<DraftIssue> get() = toDraft().issues()
    val valid: Boolean get() = issues.isEmpty()
}

/** Resolusi hasil parser (kode akun) → editable (id akun). Tanggal invalid → fallback [today]. */
fun ParsedTransaction.toEditable(idByKode: Map<String, Long>, today: LocalDate): EditableTx {
    val tgl = runCatching { LocalDate.parse(tanggal) }.getOrDefault(today)
    return EditableTx(
        tipe = tipe,
        nominal = nominal,
        kategori = kategori,
        akunDariId = akunDari?.let { idByKode[it] },
        akunKeId = akunKe?.let { idByKode[it] },
        tanggal = tgl,
        catatan = catatan,
        perluDicek = perluDicek,
    )
}

/** Editable (id akun) → kontrak parser (kode akun) untuk dioper ke layar Konfirmasi. */
fun EditableTx.toParsed(kodeById: Map<Long, String>): ParsedTransaction = ParsedTransaction(
    tipe = tipe,
    nominal = nominal,
    kategori = kategori,
    akunDari = akunDariId?.let { kodeById[it] },
    akunKe = akunKeId?.let { kodeById[it] },
    tanggal = tanggal.toString(),
    catatan = catatan,
    perluDicek = perluDicek,
)
