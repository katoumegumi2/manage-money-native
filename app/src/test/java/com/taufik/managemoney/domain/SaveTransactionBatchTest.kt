package com.taufik.managemoney.domain

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.taufik.managemoney.data.db.AppDatabase
import com.taufik.managemoney.data.db.entity.AccountEntity
import com.taufik.managemoney.data.repo.AccountRepository
import com.taufik.managemoney.data.repo.TransactionRepository
import com.taufik.managemoney.domain.model.AccountType
import com.taufik.managemoney.domain.model.TransactionDraft
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.domain.usecase.SaveTransactionBatch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SaveTransactionBatchTest {

    private lateinit var db: AppDatabase
    private lateinit var txRepo: TransactionRepository
    private lateinit var accountRepo: AccountRepository
    private lateinit var save: SaveTransactionBatch

    private val today = LocalDate.of(2026, 6, 16)
    private val bni = "id-bni"
    private val gopay = "id-gopay"

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        txRepo = TransactionRepository(db.transactionDao())
        accountRepo = AccountRepository(db.accountDao())
        save = SaveTransactionBatch(txRepo)

        db.accountDao().insert(
            AccountEntity(id = bni, kode = "bni", nama = "BNI", jenis = "bank", saldoAwal = 1_000_000, urutan = 1),
        )
        db.accountDao().insert(
            AccountEntity(id = gopay, kode = "gopay", nama = "GoPay", jenis = "ewallet", saldoAwal = 0, urutan = 2),
        )
    }

    @After
    fun tearDown() = db.close()

    private fun draft(
        tipe: TxType,
        nominal: Long,
        akunDariId: String? = null,
        akunKeId: String? = null,
        kategori: String = "lainnya",
    ) = TransactionDraft(tipe, nominal, kategori, akunDariId, akunKeId, today, "")

    private suspend fun total() = accountRepo.observeTotal().first()
    private suspend fun saldo(id: String) =
        accountRepo.observeBalances().first().first { it.accountId == id }.saldo

    @Test
    fun `simpan tiga tipe valid memperbarui saldo`() = runTest {
        val result = save(
            listOf(
                draft(TxType.PEMASUKAN, 500_000, akunKeId = bni),
                draft(TxType.PENGELUARAN, 30_000, akunDariId = bni, kategori = "makanan"),
                draft(TxType.TRANSFER, 100_000, akunDariId = bni, akunKeId = gopay),
            ),
        )
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrThrow().size)
        // bni: 1.000.000 +500.000 -30.000 -100.000 = 1.370.000
        assertEquals(1_370_000L, saldo(bni))
        assertEquals(100_000L, saldo(gopay))
        // total: 1.000.000 +500.000 -30.000 = 1.470.000 (transfer net-zero)
        assertEquals(1_470_000L, total())
    }

    @Test
    fun `transfer akun sama ditolak dan tidak menyimpan apa pun`() = runTest {
        val result = save(
            listOf(
                draft(TxType.PENGELUARAN, 30_000, akunDariId = bni),
                draft(TxType.TRANSFER, 100_000, akunDariId = bni, akunKeId = bni),
            ),
        )
        assertTrue(result.isFailure)
        // atomik: tidak ada satu pun tersimpan
        assertEquals(1_000_000L, saldo(bni))
        assertTrue(txRepo.observeRecent().first().isEmpty())
    }

    @Test
    fun `draft akun null ditolak`() = runTest {
        val result = save(listOf(draft(TxType.PENGELUARAN, 30_000, akunDariId = null)))
        assertTrue(result.isFailure)
        assertTrue(txRepo.observeRecent().first().isEmpty())
    }

    @Test
    fun `daftar kosong gagal`() = runTest {
        assertTrue(save(emptyList()).isFailure)
    }

    @Test
    fun `batch lebih dari satu berbagi batchId yang sama`() = runTest {
        save(
            listOf(
                draft(TxType.TRANSFER, 100_000, akunDariId = bni, akunKeId = gopay),
                draft(TxType.PENGELUARAN, 2_500, akunDariId = bni, kategori = "admin"),
            ),
        )
        val txs = txRepo.observeRecent().first()
        assertEquals(2, txs.size)
        val batchIds = txs.map { it.batchId }.toSet()
        assertEquals(1, batchIds.size)
        assertNotNull(batchIds.first())
    }

    @Test
    fun `transaksi tunggal batchId null`() = runTest {
        save(listOf(draft(TxType.PENGELUARAN, 30_000, akunDariId = bni)))
        val txs = txRepo.observeRecent().first()
        assertEquals(1, txs.size)
        assertNull(txs.first().batchId)
    }
}
