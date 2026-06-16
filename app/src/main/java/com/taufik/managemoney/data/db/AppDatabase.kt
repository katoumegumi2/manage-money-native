package com.taufik.managemoney.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.taufik.managemoney.data.db.dao.AccountDao
import com.taufik.managemoney.data.db.dao.TransactionDao
import com.taufik.managemoney.data.db.entity.AccountEntity
import com.taufik.managemoney.data.db.entity.TransactionEntity
import java.util.UUID

@Database(
    entities = [AccountEntity::class, TransactionEntity::class],
    version = 4,
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
                val now = System.currentTimeMillis()
                DatabaseSeed.accounts.forEach { a ->
                    db.execSQL(
                        "INSERT INTO accounts (id, kode, nama, jenis, saldoAwal, urutan, aktif, updatedAt, deletedAt) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL)",
                        arrayOf<Any>(
                            UUID.randomUUID().toString(), a.kode, a.nama, a.jenis,
                            a.saldoAwal, a.urutan, if (a.aktif) 1 else 0, now,
                        ),
                    )
                }
            }
        }
    }
}
