package com.taufik.managemoney.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

/** Migrasi skema DB. */
object Migrations {

    /**
     * Tambahkan akun katalog ([DatabaseSeed]) yang belum ada ke DB lama.
     * - INSERT OR IGNORE (kode unik) → akun & saldo milik user tetap utuh.
     * - Akun baru selalu masuk **nonaktif** (`aktif=0`): update aplikasi tidak boleh diam-diam
     *   menambah akun ke Beranda user. User mengaktifkan sendiri lewat Kelola akun.
     *   (Akun aktif default hanya berlaku untuk instalasi baru lewat onCreate seed.)
     */
    private fun seedNewAsInactive(db: SupportSQLiteDatabase) {
        DatabaseSeed.accounts.forEach { a ->
            db.execSQL(
                "INSERT OR IGNORE INTO accounts (kode, nama, jenis, saldoAwal, urutan, aktif) " +
                    "VALUES (?, ?, ?, ?, ?, 0)",
                arrayOf<Any>(a.kode, a.nama, a.jenis, a.saldoAwal, a.urutan),
            )
        }
    }

    /** v1 → v2: katalog sumber uang Indonesia (bank besar + e-wallet). */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) = seedNewAsInactive(db)
    }

    /** v2 → v3: tambahan akun (mis. Bank Jatim). Idempoten — aman dijalankan ulang. */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) = seedNewAsInactive(db)
    }

    /**
     * v3 → v4: **migrasi primary key INTEGER → UUID string** (siap sync multi-device) +
     * kolom `updatedAt`/`deletedAt`.
     *
     * Strategi: baca seluruh baris lama ke memori (volume kecil), generate UUID per akun,
     * remap FK transaksi (akunDari/akunKe) lewat peta id-lama→UUID, lalu drop & buat ulang tabel
     * dengan skema v4 dan tulis ulang data. `defer_foreign_keys` menunda cek FK sampai commit
     * sehingga drop/rename aman; di akhir seluruh FK menunjuk UUID akun yang valid.
     * Saldo derived → otomatis identik karena nominal & relasi akun dipertahankan.
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("PRAGMA defer_foreign_keys = TRUE")
            val now = System.currentTimeMillis()

            // 1. Baca akun lama → peta id lama (Long) → id baru (UUID).
            data class Acc(
                val id: String, val kode: String, val nama: String, val jenis: String,
                val saldoAwal: Long, val urutan: Int, val aktif: Int,
            )
            val idMap = HashMap<Long, String>()
            val accs = ArrayList<Acc>()
            db.query("SELECT id, kode, nama, jenis, saldoAwal, urutan, aktif FROM accounts").use { c ->
                while (c.moveToNext()) {
                    val newId = UUID.randomUUID().toString()
                    idMap[c.getLong(0)] = newId
                    accs += Acc(newId, c.getString(1), c.getString(2), c.getString(3), c.getLong(4), c.getInt(5), c.getInt(6))
                }
            }

            // 2. Baca transaksi lama → remap FK & generate id baru.
            data class Tx(
                val id: String, val tipe: String, val nominal: Long, val kategori: String,
                val akunDariId: String?, val akunKeId: String?, val tanggal: String,
                val catatan: String, val batchId: String?, val sumberInput: String, val createdAt: Long,
            )
            val txs = ArrayList<Tx>()
            db.query(
                "SELECT id, tipe, nominal, kategori, akunDariId, akunKeId, tanggal, catatan, batchId, sumberInput, createdAt FROM transactions",
            ).use { c ->
                while (c.moveToNext()) {
                    val dari = if (c.isNull(4)) null else idMap[c.getLong(4)]
                    val ke = if (c.isNull(5)) null else idMap[c.getLong(5)]
                    txs += Tx(
                        UUID.randomUUID().toString(), c.getString(1), c.getLong(2), c.getString(3),
                        dari, ke, c.getString(6), c.getString(7),
                        if (c.isNull(8)) null else c.getString(8), c.getString(9), c.getLong(10),
                    )
                }
            }

            // 3. Buang tabel lama, buat ulang dengan skema v4 (id TEXT) + index identik Room.
            db.execSQL("DROP TABLE transactions")
            db.execSQL("DROP TABLE accounts")
            db.execSQL(
                "CREATE TABLE accounts (id TEXT NOT NULL, kode TEXT NOT NULL, nama TEXT NOT NULL, " +
                    "jenis TEXT NOT NULL, saldoAwal INTEGER NOT NULL, urutan INTEGER NOT NULL, " +
                    "aktif INTEGER NOT NULL, updatedAt INTEGER NOT NULL, deletedAt INTEGER, PRIMARY KEY(id))",
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_accounts_kode ON accounts (kode)")
            db.execSQL(
                "CREATE TABLE transactions (id TEXT NOT NULL, tipe TEXT NOT NULL, nominal INTEGER NOT NULL, " +
                    "kategori TEXT NOT NULL, akunDariId TEXT, akunKeId TEXT, tanggal TEXT NOT NULL, " +
                    "catatan TEXT NOT NULL, batchId TEXT, sumberInput TEXT NOT NULL, createdAt INTEGER NOT NULL, " +
                    "updatedAt INTEGER NOT NULL, deletedAt INTEGER, PRIMARY KEY(id), " +
                    "FOREIGN KEY(akunDariId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE RESTRICT, " +
                    "FOREIGN KEY(akunKeId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE RESTRICT)",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_akunDariId ON transactions (akunDariId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_akunKeId ON transactions (akunKeId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_batchId ON transactions (batchId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_tanggal ON transactions (tanggal)")

            // 4. Tulis ulang data dengan id UUID + updatedAt; deletedAt = NULL (tombstone disiapkan).
            accs.forEach { a ->
                db.execSQL(
                    "INSERT INTO accounts (id, kode, nama, jenis, saldoAwal, urutan, aktif, updatedAt, deletedAt) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL)",
                    arrayOf<Any>(a.id, a.kode, a.nama, a.jenis, a.saldoAwal, a.urutan, a.aktif, now),
                )
            }
            txs.forEach { t ->
                db.execSQL(
                    "INSERT INTO transactions (id, tipe, nominal, kategori, akunDariId, akunKeId, tanggal, " +
                        "catatan, batchId, sumberInput, createdAt, updatedAt, deletedAt) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL)",
                    arrayOf<Any?>(
                        t.id, t.tipe, t.nominal, t.kategori, t.akunDariId, t.akunKeId, t.tanggal,
                        t.catatan, t.batchId, t.sumberInput, t.createdAt, now,
                    ),
                )
            }
        }
    }

    val ALL = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
}
