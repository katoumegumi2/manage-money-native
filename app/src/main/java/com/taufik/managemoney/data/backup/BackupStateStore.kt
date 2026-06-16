package com.taufik.managemoney.data.backup

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Metadata cadangan terakhir (untuk ditampilkan di Pengaturan). */
data class BackupMeta(val lastBackupAt: Long = 0L, val lastBackupSize: Long = 0L) {
    val hasBackup: Boolean get() = lastBackupAt > 0L
}

private val Context.backupDataStore by preferencesDataStore(name = "backup_prefs")

@Singleton
class BackupStateStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val atKey = longPreferencesKey("last_backup_at")
    private val sizeKey = longPreferencesKey("last_backup_size")
    private val restoreCheckedKey = booleanPreferencesKey("restore_checked")

    val meta: Flow<BackupMeta> = context.backupDataStore.data.map {
        BackupMeta(lastBackupAt = it[atKey] ?: 0L, lastBackupSize = it[sizeKey] ?: 0L)
    }

    /** Apakah pengecekan "ada backup di Drive?" pasca-login sudah dilakukan (gate onboarding). */
    val restoreChecked: Flow<Boolean> = context.backupDataStore.data.map { it[restoreCheckedKey] ?: false }

    suspend fun setLastBackup(at: Long, size: Long) {
        context.backupDataStore.edit {
            it[atKey] = at
            it[sizeKey] = size
        }
    }

    suspend fun setRestoreChecked(value: Boolean) {
        context.backupDataStore.edit { it[restoreCheckedKey] = value }
    }
}
