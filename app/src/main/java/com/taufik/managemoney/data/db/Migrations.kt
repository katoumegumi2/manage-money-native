package com.taufik.managemoney.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Migrasi skema DB. Skema tabel tidak berubah; tiap versi hanya menambah katalog akun bawaan. */
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

    val ALL = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
