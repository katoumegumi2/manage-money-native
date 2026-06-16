package com.taufik.managemoney.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.taufik.managemoney.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/** Baris rekap: total nominal per kategori. */
data class CategorySumRow(val kategori: String, val total: Long)

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(tx: TransactionEntity): Long

    @Update
    suspend fun update(tx: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY tanggal DESC, createdAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    /** Rekap pengeluaran per kategori sejak tanggal (untuk laporan bulan ini). */
    @Query(
        """
        SELECT kategori AS kategori, SUM(nominal) AS total FROM transactions
        WHERE tipe = 'pengeluaran' AND tanggal >= :sinceIso
        GROUP BY kategori ORDER BY total DESC
        """,
    )
    fun observeExpenseByCategorySince(sinceIso: String): Flow<List<CategorySumRow>>

    /** Tulis satu batch konfirmasi (transfer + admin, atau belanja multi-item) sekaligus. */
    @Transaction
    @Insert
    suspend fun insertAll(txs: List<TransactionEntity>): List<Long>

    @Query("SELECT * FROM transactions ORDER BY tanggal DESC, createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>

    /** Mutasi sebuah akun (sebagai sumber maupun tujuan). */
    @Query(
        """
        SELECT * FROM transactions
        WHERE akunDariId = :accountId OR akunKeId = :accountId
        ORDER BY tanggal DESC, createdAt DESC
        """,
    )
    fun observeForAccount(accountId: Long): Flow<List<TransactionEntity>>

    /**
     * Total nominal untuk satu tipe sejak tanggal tertentu (inklusif).
     * Tanggal ISO "YYYY-MM-DD" → perbandingan leksikografis = perbandingan kronologis.
     * Dipakai ringkasan masuk/keluar bulan ini di Beranda.
     */
    @Query(
        """
        SELECT COALESCE(SUM(nominal), 0) FROM transactions
        WHERE tipe = :tipe AND tanggal >= :sinceIso
        """,
    )
    fun observeSumByTypeSince(tipe: String, sinceIso: String): Flow<Long>

    @Query("SELECT * FROM transactions WHERE batchId = :batchId ORDER BY createdAt")
    suspend fun getByBatch(batchId: String): List<TransactionEntity>

    @Query("DELETE FROM transactions WHERE batchId = :batchId")
    suspend fun deleteByBatch(batchId: String)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int
}
