package com.taufik.managemoney.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.taufik.managemoney.data.db.dao.AccountDao
import com.taufik.managemoney.data.db.dao.TransactionDao
import com.taufik.managemoney.data.db.entity.AccountEntity
import com.taufik.managemoney.data.db.entity.TransactionEntity
import com.taufik.managemoney.data.repo.TransactionRepository
import com.taufik.managemoney.domain.model.AccountType
import com.taufik.managemoney.domain.model.SourceInput
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.domain.usecase.ObserveMonthlyTrend
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

/** Tren bulanan: rentang berkesinambungan, transfer dikecualikan (polish Laporan). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MonthlyTrendTest {

    private lateinit var db: AppDatabase
    private lateinit var accountDao: AccountDao
    private lateinit var txDao: TransactionDao
    private lateinit var repo: TransactionRepository
    private val bni = "id-bni"
    private val gopay = "id-gopay"

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        accountDao = db.accountDao()
        txDao = db.transactionDao()
        repo = TransactionRepository(txDao)
        accountDao.insert(account(bni, "bni", "BNI", AccountType.BANK, 0, 1))
        accountDao.insert(account(gopay, "gopay", "GoPay", AccountType.EWALLET, 0, 2))
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `tren 6 bulan berkesinambungan, transfer dikecualikan`() = runTest {
        // April: masuk 1jt; Juni: keluar 200rb + transfer 500rb (harus diabaikan)
        txDao.insert(tx(TxType.PEMASUKAN, 1_000_000, ke = bni, tanggal = "2026-04-10"))
        txDao.insert(tx(TxType.PENGELUARAN, 200_000, dari = bni, tanggal = "2026-06-05"))
        txDao.insert(tx(TxType.TRANSFER, 500_000, dari = bni, ke = gopay, tanggal = "2026-06-07"))

        val trend = ObserveMonthlyTrend(repo)
            .invoke(today = LocalDate.parse("2026-06-16"), months = 6)
            .first()

        // 6 titik: Jan..Jun 2026, urut.
        assertEquals(6, trend.size)
        assertEquals(listOf("2026-01", "2026-02", "2026-03", "2026-04", "2026-05", "2026-06"), trend.map { it.ym })

        val april = trend.first { it.ym == "2026-04" }
        assertEquals(1_000_000L, april.masuk)
        assertEquals(0L, april.keluar)

        val juni = trend.first { it.ym == "2026-06" }
        assertEquals(0L, juni.masuk)
        assertEquals(200_000L, juni.keluar) // transfer 500rb tidak terhitung

        // Bulan tanpa transaksi tetap 0 (mis. Mei).
        val mei = trend.first { it.ym == "2026-05" }
        assertEquals(0L, mei.masuk)
        assertEquals(0L, mei.keluar)
    }

    private fun account(id: String, kode: String, nama: String, jenis: AccountType, saldoAwal: Long, urutan: Int) =
        AccountEntity(id = id, kode = kode, nama = nama, jenis = jenis.wire, saldoAwal = saldoAwal, urutan = urutan)

    private fun tx(
        tipe: TxType,
        nominal: Long,
        dari: String? = null,
        ke: String? = null,
        tanggal: String,
    ) = TransactionEntity(
        id = java.util.UUID.randomUUID().toString(),
        tipe = tipe.wire, nominal = nominal, kategori = "lainnya",
        akunDariId = dari, akunKeId = ke, tanggal = tanggal, catatan = "",
        batchId = null, sumberInput = SourceInput.MANUAL.wire, createdAt = 0L,
    )
}
