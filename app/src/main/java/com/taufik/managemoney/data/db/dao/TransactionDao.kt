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

/** Baris tren: total nominal per bulan (YYYY-MM) per tipe. */
data class MonthlySumRow(val ym: String, val tipe: String, val total: Long)

@Dao
interface TransactionDao {

    // id = UUID di-generate di repo sebelum insert → return rowid tak dipakai.
    @Insert
    suspend fun insert(tx: TransactionEntity)

    @Update
    suspend fun update(tx: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY tanggal DESC, createdAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    /** Transaksi dalam rentang tanggal ISO inklusif (memakai index `tanggal`) — Riwayat & Laporan per periode. */
    @Query("SELECT * FROM transactions WHERE tanggal >= :fromIso AND tanggal <= :toIso ORDER BY tanggal DESC, createdAt DESC")
    fun observeBetween(fromIso: String, toIso: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?

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
    suspend fun insertAll(txs: List<TransactionEntity>)

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
    fun observeForAccount(accountId: String): Flow<List<TransactionEntity>>

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

    /**
     * Total nominal per bulan (YYYY-MM) untuk pemasukan & pengeluaran sejak tanggal tertentu.
     * Transfer dikecualikan (net-zero). Dipakai grafik tren bulanan di Laporan.
     */
    @Query(
        """
        SELECT substr(tanggal, 1, 7) AS ym, tipe AS tipe, SUM(nominal) AS total FROM transactions
        WHERE tipe IN ('pemasukan', 'pengeluaran') AND tanggal >= :sinceIso
        GROUP BY ym, tipe ORDER BY ym
        """,
    )
    fun observeMonthlyTotals(sinceIso: String): Flow<List<MonthlySumRow>>

    @Query("SELECT * FROM transactions WHERE batchId = :batchId ORDER BY createdAt")
    suspend fun getByBatch(batchId: String): List<TransactionEntity>

    @Query("DELETE FROM transactions WHERE batchId = :batchId")
    suspend fun deleteByBatch(batchId: String)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int
}
