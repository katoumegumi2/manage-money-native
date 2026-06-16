package com.taufik.managemoney.domain.usecase

import com.taufik.managemoney.data.repo.TransactionRepository
import com.taufik.managemoney.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Mutasi sebuah akun (sebagai sumber maupun tujuan) untuk layar Detail akun. */
class ObserveAccountTransactions @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(accountId: String): Flow<List<Transaction>> =
        transactionRepository.observeForAccount(accountId)
}
