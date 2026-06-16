package com.taufik.managemoney.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.taufik.managemoney.data.db.entity.AccountEntity
import com.taufik.managemoney.data.db.entity.TransactionEntity
import com.taufik.managemoney.domain.model.AccountType
import com.taufik.managemoney.domain.model.SourceInput
import com.taufik.managemoney.domain.model.TxType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/** Ekspor (cadangkan) lalu pulihkan file DB → data utuh (acceptance Phase 7). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseBackupTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun openDb(): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .allowMainThreadQueries().build()

    @Before
    fun clean() {
        listOf("", "-wal", "-shm").forEach { File(context.getDatabasePath(AppDatabase.NAME).path + it).delete() }
    }

    @After
    fun cleanup() = clean()

    @Test
    fun `cadangkan lalu pulihkan mengembalikan data`() = runTest {
        var db = openDb()
        val bni = "id-bni"
        val txId = "id-tx"
        db.accountDao().insert(
            AccountEntity(id = bni, kode = "bni", nama = "BNI", jenis = AccountType.BANK.wire, saldoAwal = 1_000_000, urutan = 1),
        )
        db.transactionDao().insert(
            TransactionEntity(
                id = txId,
                tipe = TxType.PENGELUARAN.wire, nominal = 25_000, kategori = "makanan",
                akunDariId = bni, akunKeId = null, tanggal = "2026-06-16", catatan = "kopi",
                batchId = null, sumberInput = SourceInput.MANUAL.wire, createdAt = 0L,
            ),
        )
        assertEquals(1, db.transactionDao().count())

        val backupFile = File.createTempFile("money-backup", ".db")
        DatabaseBackup(context, db).backup(FileOutputStream(backupFile))

        // ubah data setelah backup
        db.transactionDao().deleteById(txId)
        assertEquals(0, db.transactionDao().count())

        // pulihkan (menutup db) lalu buka ulang
        DatabaseBackup(context, db).restore(FileInputStream(backupFile))
        db = openDb()

        assertEquals(1, db.transactionDao().count())
        assertEquals(975_000L, db.accountDao().observeBalancesOnce(bni))
        db.close()
        backupFile.delete()
    }
}

/** helper: saldo satu akun. */
private suspend fun com.taufik.managemoney.data.db.dao.AccountDao.observeBalancesOnce(id: String): Long =
    observeBalances().first().first { it.accountId == id }.saldo
