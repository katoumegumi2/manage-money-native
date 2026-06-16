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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BalanceTest {

    private lateinit var db: AppDatabase
    private lateinit var accountDao: AccountDao
    private lateinit var txDao: TransactionDao

    // id akun (UUID-string; pakai kode sebagai id agar mudah dibaca di test)
    private val bni = "id-bni"
    private val tunai = "id-tunai"
    private val gopay = "id-gopay"

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        accountDao = db.accountDao()
        txDao = db.transactionDao()

        accountDao.insert(account(bni, "bni", "BNI", AccountType.BANK, saldoAwal = 1_000_000, urutan = 1))
        accountDao.insert(account(tunai, "tunai", "Tunai", AccountType.TUNAI, saldoAwal = 50_000, urutan = 2))
        accountDao.insert(account(gopay, "gopay", "GoPay", AccountType.EWALLET, saldoAwal = 0, urutan = 3))
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `saldo awal tanpa transaksi`() = runTest {
        assertEquals(1_050_000L, total())
        assertEquals(1_000_000L, saldo(bni))
        assertEquals(50_000L, saldo(tunai))
    }

    @Test
    fun `pengeluaran mengurangi akun sumber dan total`() = runTest {
        txDao.insert(tx(TxType.PENGELUARAN, 25_000, akunDari = bni))
        assertEquals(975_000L, saldo(bni))
        assertEquals(1_025_000L, total())
    }

    @Test
    fun `pemasukan menambah akun tujuan dan total`() = runTest {
        txDao.insert(tx(TxType.PEMASUKAN, 8_000_000, akunKe = bni))
        assertEquals(9_000_000L, saldo(bni))
        assertEquals(9_050_000L, total())
    }

    @Test
    fun `transfer net-zero terhadap total`() = runTest {
        // tarik tunai 300rb dari BNI
        txDao.insert(tx(TxType.TRANSFER, 300_000, akunDari = bni, akunKe = tunai))
        assertEquals(700_000L, saldo(bni))
        assertEquals(350_000L, saldo(tunai))
        assertEquals(1_050_000L, total()) // total tidak berubah
    }

    @Test
    fun `admin adalah pengeluaran terpisah yang mengurangi total`() = runTest {
        // top up gopay 100rb dari BNI + admin 2500 dari BNI
        txDao.insertAll(
            listOf(
                tx(TxType.TRANSFER, 100_000, akunDari = bni, akunKe = gopay, batchId = "b1"),
                tx(TxType.PENGELUARAN, 2_500, akunDari = bni, kategori = "admin", batchId = "b1"),
            ),
        )
        assertEquals(897_500L, saldo(bni)) // 1.000.000 - 100.000 - 2.500
        assertEquals(100_000L, saldo(gopay))
        assertEquals(1_047_500L, total()) // turun 2.500 karena admin
    }

    @Test
    fun `banyak transaksi campuran`() = runTest {
        txDao.insertAll(
            listOf(
                tx(TxType.PEMASUKAN, 5_000_000, akunKe = bni),       // total +5jt
                tx(TxType.PENGELUARAN, 1_000_000, akunDari = bni),   // total -1jt
                tx(TxType.TRANSFER, 200_000, akunDari = bni, akunKe = gopay), // net-zero
                tx(TxType.PENGELUARAN, 45_000, akunDari = gopay),    // total -45rb
            ),
        )
        // bni: 1.000.000 +5.000.000 -1.000.000 -200.000 = 4.800.000
        assertEquals(4_800_000L, saldo(bni))
        // gopay: 0 +200.000 -45.000 = 155.000
        assertEquals(155_000L, saldo(gopay))
        // total: 1.050.000 +5.000.000 -1.000.000 -45.000 = 5.005.000
        assertEquals(5_005_000L, total())
    }

    // --- helpers ---

    private suspend fun total(): Long = accountDao.observeTotal().first()

    private suspend fun saldo(accountId: String): Long =
        accountDao.observeBalances().first().first { it.accountId == accountId }.saldo

    private fun account(id: String, kode: String, nama: String, jenis: AccountType, saldoAwal: Long, urutan: Int) =
        AccountEntity(id = id, kode = kode, nama = nama, jenis = jenis.wire, saldoAwal = saldoAwal, urutan = urutan)

    private fun tx(
        tipe: TxType,
        nominal: Long,
        akunDari: String? = null,
        akunKe: String? = null,
        kategori: String = "lainnya",
        batchId: String? = null,
    ) = TransactionEntity(
        id = java.util.UUID.randomUUID().toString(),
        tipe = tipe.wire,
        nominal = nominal,
        kategori = kategori,
        akunDariId = akunDari,
        akunKeId = akunKe,
        tanggal = "2026-06-15",
        catatan = "",
        batchId = batchId,
        sumberInput = SourceInput.MANUAL.wire,
        createdAt = 0L,
    )
}
