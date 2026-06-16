package com.taufik.managemoney.data.db

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cadangkan / pulihkan file database lokal (money.db) ke/dari stream (mis. file pilihan SAF).
 * Backup: checkpoint WAL → salin file. Restore: tutup DB → timpa file → buang WAL/SHM.
 * Setelah restore, aplikasi WAJIB di-restart (DB sudah ditutup) — UI yang memanggil [restartHint].
 */
@Singleton
class DatabaseBackup @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
) {
    private fun dbFile(): File = context.getDatabasePath(AppDatabase.NAME)

    private fun checkpoint() {
        database.openHelper.writableDatabase
            .query("PRAGMA wal_checkpoint(TRUNCATE)")
            .use { it.moveToFirst() }
    }

    suspend fun backup(output: OutputStream) = withContext(Dispatchers.IO) {
        checkpoint()
        FileInputStream(dbFile()).use { it.copyTo(output) }
    }

    /** Timpa DB dengan isi [input]. Setelah ini DB tertutup; panggil restart aplikasi. */
    suspend fun restore(input: InputStream) = withContext(Dispatchers.IO) {
        checkpoint()
        database.close()
        val target = dbFile()
        FileOutputStream(target).use { out -> input.copyTo(out) }
        File(target.path + "-wal").delete()
        File(target.path + "-shm").delete()
    }
}
