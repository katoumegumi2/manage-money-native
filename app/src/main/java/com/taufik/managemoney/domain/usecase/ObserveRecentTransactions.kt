package com.taufik.managemoney.domain.usecase

import com.taufik.managemoney.data.repo.TransactionRepository
import com.taufik.managemoney.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Transaksi terbaru lintas akun untuk daftar di Beranda. */
class ObserveRecentTransactions @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(limit: Int = 10): Flow<List<Transaction>> =
        transactionRepository.observeRecent(limit)
}
