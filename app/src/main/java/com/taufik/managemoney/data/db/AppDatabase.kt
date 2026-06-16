package com.taufik.managemoney.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.taufik.managemoney.data.db.dao.AccountDao
import com.taufik.managemoney.data.db.dao.TransactionDao
import com.taufik.managemoney.data.db.entity.AccountEntity
import com.taufik.managemoney.data.db.entity.TransactionEntity

@Database(
    entities = [AccountEntity::class, TransactionEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val NAME = "money.db"

        /** Seed katalog akun bawaan saat DB pertama dibuat (sekali jalan). */
        val seedCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                DatabaseSeed.accounts.forEach { a ->
                    db.execSQL(
                        "INSERT INTO accounts (kode, nama, jenis, saldoAwal, urutan, aktif) VALUES (?, ?, ?, ?, ?, ?)",
                        arrayOf<Any>(a.kode, a.nama, a.jenis, a.saldoAwal, a.urutan, if (a.aktif) 1 else 0),
                    )
                }
            }
        }
    }
}
