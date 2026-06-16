package com.taufik.managemoney.data.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Auto-backup terenkripsi ke Drive (periodik harian + dipicu setelah simpan transaksi).
 * Diam-diam (tanpa UI): perlu passphrase tersimpan + token Drive silent. Jika belum siap → dilewati.
 */
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val driveAuthManager: DriveAuthManager,
    private val passphraseStore: PassphraseStore,
    private val backupRepository: BackupRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!passphraseStore.has()) return Result.success() // belum aktif → tak perlu retry
        val token = driveAuthManager.silentToken() ?: return Result.retry()
        return backupRepository.backup(token).fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }
}

/** Jadwalkan & picu auto-backup. */
object BackupScheduler {
    private const val DAILY = "drive-backup-daily"
    private const val NOW = "drive-backup-now"

    private fun constraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun scheduleDaily(context: Context) {
        val request = PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(DAILY, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /** Backup sekali jalan (mis. setelah simpan batch transaksi). */
    fun backupNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(constraints())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(NOW, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY)
    }
}
