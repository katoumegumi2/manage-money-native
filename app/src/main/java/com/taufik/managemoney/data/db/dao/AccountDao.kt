package com.taufik.managemoney.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taufik.managemoney.data.db.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

/** Baris hasil hitung saldo per akun (saldoAwal + masuk − keluar). */
data class AccountBalanceRow(
    val accountId: Long,
    val kode: String,
    val nama: String,
    val jenis: String,
    val saldo: Long,
)

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE aktif = 1 ORDER BY urutan")
    fun observeActive(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY urutan")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE kode = :kode LIMIT 1")
    suspend fun getByKode(kode: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun count(): Int

    /** Jumlah akun aktif — untuk jaga minimal 1 akun aktif tersisa. */
    @Query("SELECT COUNT(*) FROM accounts WHERE aktif = 1")
    fun observeActiveCount(): kotlinx.coroutines.flow.Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(accounts: List<AccountEntity>): List<Long>

    @Update
    suspend fun update(account: AccountEntity)

    /**
     * Saldo tiap akun aktif = saldoAwal + Σ(masuk) − Σ(keluar). Lihat desain-data-keuangan.md.
     */
    @Query(
        """
        SELECT a.id AS accountId, a.kode AS kode, a.nama AS nama, a.jenis AS jenis,
            a.saldoAwal
            + COALESCE((SELECT SUM(t.nominal) FROM transactions t WHERE t.akunKeId   = a.id), 0)
            - COALESCE((SELECT SUM(t.nominal) FROM transactions t WHERE t.akunDariId = a.id), 0) AS saldo
        FROM accounts a
        WHERE a.aktif = 1
        ORDER BY a.urutan
        """,
    )
    fun observeBalances(): Flow<List<AccountBalanceRow>>

    /** Saldo SEMUA akun (aktif + nonaktif) — untuk layar Kelola akun. */
    @Query(
        """
        SELECT a.id AS accountId, a.kode AS kode, a.nama AS nama, a.jenis AS jenis,
            a.saldoAwal
            + COALESCE((SELECT SUM(t.nominal) FROM transactions t WHERE t.akunKeId   = a.id), 0)
            - COALESCE((SELECT SUM(t.nominal) FROM transactions t WHERE t.akunDariId = a.id), 0) AS saldo
        FROM accounts a
        ORDER BY a.urutan
        """,
    )
    fun observeAllBalances(): Flow<List<AccountBalanceRow>>

    /** Total = Σ saldo seluruh akun aktif. Transfer antar akun aktif net-zero; admin mengurangi total. */
    @Query(
        """
        SELECT
            COALESCE((SELECT SUM(saldoAwal) FROM accounts WHERE aktif = 1), 0)
            + COALESCE((SELECT SUM(t.nominal) FROM transactions t
                        JOIN accounts a ON t.akunKeId = a.id WHERE a.aktif = 1), 0)
            - COALESCE((SELECT SUM(t.nominal) FROM transactions t
                        JOIN accounts a ON t.akunDariId = a.id WHERE a.aktif = 1), 0)
        """,
    )
    fun observeTotal(): Flow<Long>
}
