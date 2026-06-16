package com.taufik.managemoney.domain.model

import java.time.LocalDate

/**
 * Draft transaksi yang sedang dikonfirmasi/diisi user. Akun sudah di-resolve ke FK id
 * (atau null bila belum dipilih). Dipakai layar Konfirmasi & Manual entry sebelum disimpan.
 */
data class TransactionDraft(
    val tipe: TxType,
    val nominal: Long,
    val kategori: String,
    val akunDariId: String?,
    val akunKeId: String?,
    val tanggal: LocalDate,
    val catatan: String = "",
)

/** Pelanggaran aturan simpan §4.3 (jaga integritas saldo). Kosong = boleh disimpan. */
enum class DraftIssue {
    NOMINAL_TIDAK_VALID, // nominal <= 0
    AKUN_DARI_WAJIB, // pengeluaran/transfer tanpa akun "dari"
    AKUN_KE_WAJIB, // pemasukan/transfer tanpa akun "ke"
    TRANSFER_AKUN_SAMA, // transfer dengan akun dari == ke
}

/**
 * Aturan simpan §4.3 — validator murni:
 * - pengeluaran → akunDari wajib
 * - pemasukan → akunKe wajib
 * - transfer → keduanya wajib & berbeda
 * - nominal > 0
 */
fun TransactionDraft.issues(): Set<DraftIssue> {
    val issues = mutableSetOf<DraftIssue>()
    if (nominal <= 0) issues += DraftIssue.NOMINAL_TIDAK_VALID
    when (tipe) {
        TxType.PENGELUARAN -> if (akunDariId == null) issues += DraftIssue.AKUN_DARI_WAJIB
        TxType.PEMASUKAN -> if (akunKeId == null) issues += DraftIssue.AKUN_KE_WAJIB
        TxType.TRANSFER -> {
            if (akunDariId == null) issues += DraftIssue.AKUN_DARI_WAJIB
            if (akunKeId == null) issues += DraftIssue.AKUN_KE_WAJIB
            if (akunDariId != null && akunKeId != null && akunDariId == akunKeId) {
                issues += DraftIssue.TRANSFER_AKUN_SAMA
            }
        }
    }
    return issues
}

/** True bila draft lolos seluruh aturan simpan. */
fun TransactionDraft.isValid(): Boolean = issues().isEmpty()

/**
 * Bangun [Transaction] siap simpan. Akun dinormalkan per tipe agar tidak ada FK nyasar:
 * pengeluaran hanya pakai akunDari, pemasukan hanya akunKe, transfer keduanya.
 * Panggil hanya untuk draft yang sudah valid ([isValid]).
 */
fun TransactionDraft.toTransaction(
    batchId: String?,
    sumber: SourceInput,
    createdAt: Long = System.currentTimeMillis(),
): Transaction {
    val dari = if (tipe == TxType.PEMASUKAN) null else akunDariId
    val ke = if (tipe == TxType.PENGELUARAN) null else akunKeId
    return Transaction(
        tipe = tipe,
        nominal = nominal,
        kategori = kategori,
        akunDariId = dari,
        akunKeId = ke,
        tanggal = tanggal,
        catatan = catatan,
        batchId = batchId,
        sumberInput = sumber,
        createdAt = createdAt,
    )
}
