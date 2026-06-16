package com.taufik.managemoney.data.repo

import com.taufik.managemoney.data.db.dao.AccountDao
import com.taufik.managemoney.data.db.toDomain
import com.taufik.managemoney.data.db.toEntity
import com.taufik.managemoney.domain.model.Account
import com.taufik.managemoney.domain.model.AccountBalance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
) {
    fun observeActiveAccounts(): Flow<List<Account>> =
        accountDao.observeActive().map { list -> list.map { it.toDomain() } }

    /** Semua akun (aktif + nonaktif) — untuk Kelola akun. */
    fun observeAllAccounts(): Flow<List<Account>> =
        accountDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeBalances(): Flow<List<AccountBalance>> =
        accountDao.observeBalances().map { rows -> rows.map { it.toDomain() } }

    /** Saldo semua akun (aktif + nonaktif) — untuk Kelola akun. */
    fun observeAllBalances(): Flow<List<AccountBalance>> =
        accountDao.observeAllBalances().map { rows -> rows.map { it.toDomain() } }

    fun observeActiveCount(): Flow<Int> = accountDao.observeActiveCount()

    fun observeTotal(): Flow<Long> = accountDao.observeTotal()

    suspend fun getByKode(kode: String): Account? = accountDao.getByKode(kode)?.toDomain()

    suspend fun add(account: Account): Long = accountDao.insert(account.toEntity())

    suspend fun update(account: Account) = accountDao.update(account.toEntity())
}
