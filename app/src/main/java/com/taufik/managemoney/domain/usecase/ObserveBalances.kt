package com.taufik.managemoney.domain.usecase

import com.taufik.managemoney.data.repo.AccountRepository
import com.taufik.managemoney.domain.model.AccountBalance
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Saldo tiap akun aktif, reaktif terhadap perubahan ledger. */
class ObserveBalances @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    operator fun invoke(): Flow<List<AccountBalance>> = accountRepository.observeBalances()
}
