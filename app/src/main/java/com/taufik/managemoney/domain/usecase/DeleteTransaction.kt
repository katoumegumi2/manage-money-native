package com.taufik.managemoney.domain.usecase

import com.taufik.managemoney.data.repo.TransactionRepository
import javax.inject.Inject

/** Hapus satu transaksi. Saldo otomatis menyesuaikan (derived). */
class DeleteTransaction @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(id: Long) = transactionRepository.deleteById(id)
}

/** Hapus seluruh transaksi satu batch konfirmasi (mis. transfer + admin). */
class DeleteTransactionBatch @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(batchId: String) = transactionRepository.deleteByBatch(batchId)
}
