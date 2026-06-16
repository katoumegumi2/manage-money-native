package com.taufik.managemoney.domain.usecase

import com.taufik.managemoney.data.repo.TransactionRepository
import com.taufik.managemoney.domain.model.DraftIssue
import com.taufik.managemoney.domain.model.SourceInput
import com.taufik.managemoney.domain.model.TransactionDraft
import com.taufik.managemoney.domain.model.issues
import com.taufik.managemoney.domain.model.toTransaction
import java.util.UUID
import javax.inject.Inject

/** Daftar draft kosong tak bisa disimpan. */
class EmptyBatchException : IllegalArgumentException("Tidak ada transaksi untuk disimpan")

/** Ada draft yang melanggar aturan simpan §4.3 (index → kumpulan masalah). */
class InvalidDraftException(
    val problems: Map<Int, Set<DraftIssue>>,
) : IllegalArgumentException("Ada transaksi yang belum lengkap")

/**
 * Simpan satu batch draft secara atomik:
 * - semua draft WAJIB valid (§4.3); kalau ada satu yang invalid → gagal, tidak menyimpan apa pun.
 * - batch >1 transaksi diberi [batchId] sama (UUID); transaksi tunggal → batchId null.
 */
class SaveTransactionBatch @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(
        drafts: List<TransactionDraft>,
        sumber: SourceInput = SourceInput.MANUAL,
    ): Result<List<String>> {
        if (drafts.isEmpty()) return Result.failure(EmptyBatchException())

        val problems = drafts.mapIndexedNotNull { index, draft ->
            draft.issues().takeIf { it.isNotEmpty() }?.let { index to it }
        }.toMap()
        if (problems.isNotEmpty()) return Result.failure(InvalidDraftException(problems))

        val batchId = if (drafts.size > 1) UUID.randomUUID().toString() else null
        val txs = drafts.map { it.toTransaction(batchId = batchId, sumber = sumber) }
        return runCatching { transactionRepository.insertBatch(txs) }
    }
}
