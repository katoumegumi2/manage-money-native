package com.taufik.managemoney.data.repo

import com.taufik.managemoney.data.db.dao.TransactionDao
import com.taufik.managemoney.data.db.toDomain
import com.taufik.managemoney.data.db.toEntity
import com.taufik.managemoney.domain.model.Transaction
import com.taufik.managemoney.domain.model.TxType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
) {
    fun observeRecent(limit: Int = 20): Flow<List<Transaction>> =
        transactionDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    fun observeForAccount(accountId: Long): Flow<List<Transaction>> =
        transactionDao.observeForAccount(accountId).map { list -> list.map { it.toDomain() } }

    /** Total nominal satu tipe sejak tanggal ISO (inklusif) — ringkasan masuk/keluar. */
    fun observeSumByTypeSince(tipe: TxType, sinceIso: String): Flow<Long> =
        transactionDao.observeSumByTypeSince(tipe.wire, sinceIso)

    suspend fun insert(tx: Transaction): Long = transactionDao.insert(tx.toEntity())

    /** Simpan satu batch (semua transaksi dari 1 konfirmasi) dengan batchId yang sama. */
    suspend fun insertBatch(txs: List<Transaction>): List<Long> =
        transactionDao.insertAll(txs.map { it.toEntity() })

    suspend fun deleteByBatch(batchId: String) = transactionDao.deleteByBatch(batchId)

    suspend fun deleteById(id: Long) = transactionDao.deleteById(id)
}
