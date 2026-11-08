package com.taufik.managemoney.data.repo

import com.taufik.managemoney.data.db.dao.TransactionDao
import com.taufik.managemoney.data.db.toDomain
import com.taufik.managemoney.data.db.toEntity
import com.taufik.managemoney.domain.model.CategorySum
import com.taufik.managemoney.domain.model.MonthlyTotal
import com.taufik.managemoney.domain.model.Transaction
import com.taufik.managemoney.domain.model.TxType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
) {
    fun observeRecent(limit: Int = 20): Flow<List<Transaction>> =
        transactionDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    fun observeForAccount(accountId: String): Flow<List<Transaction>> =
        transactionDao.observeForAccount(accountId).map { list -> list.map { it.toDomain() } }

    fun observeAll(): Flow<List<Transaction>> =
        transactionDao.observeAll().map { list -> list.map { it.toDomain() } }
            .flowOn(kotlinx.coroutines.Dispatchers.Default)

    /** Transaksi dalam rentang [from]..[to] (inklusif). Pemetaan entity→domain di luar thread UI. */
    fun observeBetween(from: java.time.LocalDate, to: java.time.LocalDate): Flow<List<Transaction>> =
        transactionDao.observeBetween(from.toString(), to.toString()).map { list -> list.map { it.toDomain() } }
            .flowOn(kotlinx.coroutines.Dispatchers.Default)

    suspend fun getById(id: String): Transaction? = transactionDao.getById(id)?.toDomain()

    suspend fun update(tx: Transaction) = transactionDao.update(tx.toEntity())

    /** Rekap pengeluaran per kategori sejak tanggal ISO (laporan). */
    fun observeExpenseByCategorySince(sinceIso: String): Flow<List<CategorySum>> =
        transactionDao.observeExpenseByCategorySince(sinceIso)
            .map { rows -> rows.map { CategorySum(it.kategori, it.total) } }

    /** Total nominal satu tipe sejak tanggal ISO (inklusif) — ringkasan masuk/keluar. */
    fun observeSumByTypeSince(tipe: TxType, sinceIso: String): Flow<Long> =
        transactionDao.observeSumByTypeSince(tipe.wire, sinceIso)

    /** Total masuk/keluar per bulan sejak tanggal ISO — grafik tren bulanan. */
    fun observeMonthlyTotals(sinceIso: String): Flow<List<MonthlyTotal>> =
        transactionDao.observeMonthlyTotals(sinceIso)
            .map { rows -> rows.map { MonthlyTotal(it.ym, TxType.fromWire(it.tipe), it.total) } }

    /** Simpan satu transaksi; generate UUID bila id belum diisi. Mengembalikan id final. */
    suspend fun insert(tx: Transaction): String {
        val entity = tx.toEntity().ensureId()
        transactionDao.insert(entity)
        return entity.id
    }

    /** Simpan satu batch (semua transaksi dari 1 konfirmasi) dengan batchId yang sama. */
    suspend fun insertBatch(txs: List<Transaction>): List<String> {
        val entities = txs.map { it.toEntity().ensureId() }
        transactionDao.insertAll(entities)
        return entities.map { it.id }
    }

    suspend fun deleteByBatch(batchId: String) = transactionDao.deleteByBatch(batchId)

    suspend fun deleteById(id: String) = transactionDao.deleteById(id)
}

/** Pastikan entity punya id UUID (generate bila kosong). */
private fun com.taufik.managemoney.data.db.entity.TransactionEntity.ensureId() =
    if (id.isBlank()) copy(id = java.util.UUID.randomUUID().toString()) else this
