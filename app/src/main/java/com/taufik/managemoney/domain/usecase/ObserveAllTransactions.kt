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
}
