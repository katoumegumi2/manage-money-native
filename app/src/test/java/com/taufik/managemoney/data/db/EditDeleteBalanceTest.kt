package com.taufik.managemoney.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.taufik.managemoney.data.db.dao.AccountDao
import com.taufik.managemoney.data.db.dao.TransactionDao
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

/** Edit & hapus transaksi → saldo derived menyesuaikan (acceptance Phase 7). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EditDeleteBalanceTest {

    private lateinit var db: AppDatabase
    private lateinit var accountDao: AccountDao
    private lateinit var txDao: TransactionDao
    private var bni = 0L
    private var gopay = 0L

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        accountDao = db.accountDao()
        txDao = db.transactionDao()
        bni = accountDao.insert(account("bni", "BNI", AccountType.BANK, 1_000_000, 1))
        gopay = accountDao.insert(account("gopay", "GoPay", AccountType.EWALLET, 0, 2))
    }

    @After
    fun tearDown() = db.close()

    private suspend fun saldo(id: Long) =
        accountDao.observeBalances().first().first { it.accountId == id }.saldo

    private suspend fun total() = accountDao.observeTotal().first()

    @Test
    fun `edit nominal memperbarui saldo`() = runTest {
        val id = txDao.insert(tx(TxType.PENGELUARAN, 25_000, dari = bni))
        assertEquals(975_000L, saldo(bni))

        val current = txDao.getById(id)!!
        txDao.update(current.copy(nominal = 50_000))
        assertEquals(950_000L, saldo(bni))
    }

    @Test
    fun `edit pindah akun memindah saldo`() = runTest {
        val id = txDao.insert(tx(TxType.PENGELUARAN, 30_000, dari = bni))
        val current = txDao.getById(id)!!
        txDao.update(current.copy(akunDariId = gopay))
        assertEquals(1_000_000L, saldo(bni)) // bni kembali utuh
        assertEquals(-30_000L, saldo(gopay)) // gopay turun
    }

    @Test
    fun `hapus transaksi mengembalikan saldo`() = runTest {
        val id = txDao.insert(tx(TxType.PENGELUARAN, 25_000, dari = bni))
        assertEquals(975_000L, saldo(bni))
        txDao.deleteById(id)
        assertEquals(1_000_000L, saldo(bni))
    }

    @Test
    fun `hapus batch mengembalikan saldo`() = runTest {
        txDao.insertAll(
            listOf(
                tx(TxType.TRANSFER, 100_000, dari = bni, ke = gopay, batchId = "b1"),
                tx(TxType.PENGELUARAN, 2_500, dari = bni, kategori = "admin", batchId = "b1"),
            ),
        )
        assertEquals(897_500L, saldo(bni))
        txDao.deleteByBatch("b1")
        assertEquals(1_000_000L, saldo(bni))
        assertEquals(0L, saldo(gopay))
        assertEquals(1_000_000L, total())
    }

    private fun account(kode: String, nama: String, jenis: AccountType, saldoAwal: Long, urutan: Int) =
        AccountEntity(kode = kode, nama = nama, jenis = jenis.wire, saldoAwal = saldoAwal, urutan = urutan)

    private fun tx(
        tipe: TxType,
        nominal: Long,
        dari: Long? = null,
        ke: Long? = null,
        kategori: String = "lainnya",
        batchId: String? = null,
    ) = TransactionEntity(
        tipe = tipe.wire, nominal = nominal, kategori = kategori,
        akunDariId = dari, akunKeId = ke, tanggal = "2026-06-16", catatan = "",
        batchId = batchId, sumberInput = SourceInput.MANUAL.wire, createdAt = 0L,
    )
}
