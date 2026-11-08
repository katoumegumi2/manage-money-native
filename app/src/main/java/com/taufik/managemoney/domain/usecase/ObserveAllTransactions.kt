package com.taufik.managemoney.domain.usecase

import com.taufik.managemoney.data.repo.TransactionRepository
import com.taufik.managemoney.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Semua transaksi (terbaru dulu) untuk layar Riwayat. */
class ObserveAllTransactions @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(): Flow<List<Transaction>> = transactionRepository.observeAll()

    /** [from] null = seluruh riwayat. */
    operator fun invoke(from: java.time.LocalDate?, to: java.time.LocalDate): Flow<List<Transaction>> =
        if (from == null) transactionRepository.observeAll() else transactionRepository.observeBetween(from, to)
}
