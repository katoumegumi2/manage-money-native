package com.taufik.managemoney.domain.usecase

import com.taufik.managemoney.data.repo.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Total saldo seluruh akun aktif. Transfer net-zero; admin mengurangi total. */
class ObserveTotal @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    operator fun invoke(): Flow<Long> = accountRepository.observeTotal()
}
