package com.taufik.managemoney.data.repo

import com.taufik.managemoney.data.db.dao.AccountDao
import com.taufik.managemoney.data.db.toDomain
import com.taufik.managemoney.data.db.toEntity
import com.taufik.managemoney.domain.model.Account
import com.taufik.managemoney.domain.model.AccountBalance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
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

    /** Tambah akun baru; generate UUID bila id belum diisi. Mengembalikan id final. */
    suspend fun add(account: Account): String {
        val id = account.id.ifBlank { UUID.randomUUID().toString() }
        accountDao.insert(account.toEntity().copy(id = id))
        return id
    }

    suspend fun update(account: Account) = accountDao.update(account.toEntity())
}
