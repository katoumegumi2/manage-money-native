package com.taufik.managemoney.domain.usecase

import com.taufik.managemoney.data.repo.AccountRepository
import com.taufik.managemoney.domain.model.Account
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Daftar akun aktif (meta: nama, jenis, saldo awal, urutan). */
class ObserveAccounts @Inject constructor(
    private val accountRepository: AccountRepository,
) {
    operator fun invoke(): Flow<List<Account>> = accountRepository.observeActiveAccounts()
}
