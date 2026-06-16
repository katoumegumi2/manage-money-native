package com.taufik.managemoney.data.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.taufik.managemoney.data.db.dao.AccountBalanceRow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Migrasi v3 → v4: primary key INTEGER → UUID string. Verifikasi data utuh, FK ter-remap, saldo identik.
 * Membangun DB v3 manual (skema lama) lalu membukanya dengan Room + [Migrations.ALL].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Migration3to4Test {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "migration-3to4-test.db"

    @Before
    fun clean() {
        listOf("", "-wal", "-shm").forEach { context.getDatabasePath(dbName).path.let { p -> java.io.File(p + it).delete() } }
    }

    @After
    fun cleanup() = clean()

    /** Buat file DB skema v3 (id INTEGER) berisi 2 akun + 3 transaksi (termasuk transfer). */
    private fun createV3Database() {
        val callback = object : SupportSQLiteOpenHelper.Callback(3) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE accounts (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, kode TEXT NOT NULL, " +
                        "nama TEXT NOT NULL, jenis TEXT NOT NULL, saldoAwal INTEGER NOT NULL, urutan INTEGER NOT NULL, " +
                        "aktif INTEGER NOT NULL)",
                )
                db.execSQL("CREATE UNIQUE INDEX index_accounts_kode ON accounts (kode)")
                db.execSQL(
                    "CREATE TABLE transactions (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, tipe TEXT NOT NULL, " +
                        "nominal INTEGER NOT NULL, kategori TEXT NOT NULL, akunDariId INTEGER, akunKeId INTEGER, " +
                        "tanggal TEXT NOT NULL, catatan TEXT NOT NULL, batchId TEXT, sumberInput TEXT NOT NULL, " +
                        "createdAt INTEGER NOT NULL, " +
                        "FOREIGN KEY(akunDariId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE RESTRICT, " +
                        "FOREIGN KEY(akunKeId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE RESTRICT)",
                )
                db.execSQL("CREATE INDEX index_transactions_akunDariId ON transactions (akunDariId)")
                db.execSQL("CREATE INDEX index_transactions_akunKeId ON transactions (akunKeId)")
                db.execSQL("CREATE INDEX index_transactions_batchId ON transactions (batchId)")
                db.execSQL("CREATE INDEX index_transactions_tanggal ON transactions (tanggal)")

                // akun: 1=BNI (saldoAwal 1jt), 2=GoPay (0)
                db.execSQL("INSERT INTO accounts (id, kode, nama, jenis, saldoAwal, urutan, aktif) VALUES (1, 'bni', 'BNI', 'bank', 1000000, 1, 1)")
                db.execSQL("INSERT INTO accounts (id, kode, nama, jenis, saldoAwal, urutan, aktif) VALUES (2, 'gopay', 'GoPay', 'ewallet', 0, 2, 1)")
                // transaksi: pengeluaran 25rb dari BNI; transfer 100rb BNI→GoPay; pemasukan 50rb ke GoPay
                db.execSQL("INSERT INTO transactions (tipe, nominal, kategori, akunDariId, akunKeId, tanggal, catatan, batchId, sumberInput, createdAt) VALUES ('pengeluaran', 25000, 'makanan', 1, NULL, '2026-06-10', 'kopi', NULL, 'manual', 0)")
                db.execSQL("INSERT INTO transactions (tipe, nominal, kategori, akunDariId, akunKeId, tanggal, catatan, batchId, sumberInput, createdAt) VALUES ('transfer', 100000, 'lainnya', 1, 2, '2026-06-11', '', 'b1', 'manual', 0)")
                db.execSQL("INSERT INTO transactions (tipe, nominal, kategori, akunDariId, akunKeId, tanggal, catatan, batchId, sumberInput, createdAt) VALUES ('pemasukan', 50000, 'bonus', NULL, 2, '2026-06-12', '', NULL, 'manual', 0)")
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(callback)
            .build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        helper.writableDatabase // memicu onCreate
        helper.close()
    }

    @Test
    fun `migrasi v3 ke v4 - data utuh, FK ter-remap, saldo identik`() = runTest {
        createV3Database()

        val db = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(*Migrations.ALL)
            .allowMainThreadQueries()
            .build()

        // 1. Jumlah baris dipertahankan.
        assertEquals(2, db.accountDao().count())
        assertEquals(3, db.transactionDao().count())

        // 2. Semua id akun kini UUID (bukan angka lama "1"/"2").
        val accounts = db.accountDao().observeAll().first()
        val idByKode = accounts.associate { it.kode to it.id }
        accounts.forEach { assertTrue("id harus UUID non-kosong", it.id.length >= 32) }

        // 3. FK transaksi ter-remap ke UUID akun yang benar.
        val txs = db.transactionDao().observeAll().first()
        val transfer = txs.first { it.tipe == "transfer" }
        assertEquals(idByKode["bni"], transfer.akunDariId)
        assertEquals(idByKode["gopay"], transfer.akunKeId)

        // 4. Saldo derived identik: BNI 1jt -25rb -100rb = 875rb; GoPay +100rb +50rb = 150rb.
        val balances: List<AccountBalanceRow> = db.accountDao().observeAllBalances().first()
        fun saldo(kode: String) = balances.first { it.accountId == idByKode[kode] }.saldo
        assertEquals(875_000L, saldo("bni"))
        assertEquals(150_000L, saldo("gopay"))
        assertEquals(1_025_000L, db.accountDao().observeTotal().first())

        db.close()
    }
}
