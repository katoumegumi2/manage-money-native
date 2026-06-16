package com.taufik.managemoney.domain.usecase

import com.taufik.managemoney.data.repo.TransactionRepository
import com.taufik.managemoney.domain.model.Transaction
import com.taufik.managemoney.domain.model.TransactionDraft
import com.taufik.managemoney.domain.model.issues
import com.taufik.managemoney.domain.model.toTransaction
import javax.inject.Inject

/**
 * Edit transaksi tersimpan. Validasi aturan §4.3 (sama seperti simpan); akun dinormalkan per tipe.
 * id/batchId/sumber/createdAt dipertahankan. Saldo otomatis ikut karena derived dari ledger.
 */
class UpdateTransaction @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(tx: Transaction): Result<Unit> {
        val draft = TransactionDraft(
            tipe = tx.tipe,
            nominal = tx.nominal,
            kategori = tx.kategori,
            akunDariId = tx.akunDariId,
            akunKeId = tx.akunKeId,
            tanggal = tx.tanggal,
            catatan = tx.catatan,
        )
        val issues = draft.issues()
        if (issues.isNotEmpty()) return Result.failure(InvalidDraftException(mapOf(0 to issues)))

        val normalized = draft
            .toTransaction(batchId = tx.batchId, sumber = tx.sumberInput, createdAt = tx.createdAt)
            .copy(id = tx.id)
        return runCatching { transactionRepository.update(normalized) }
    }
}
