package com.taufik.managemoney.data.backup

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Hasil minta otorisasi Drive. */
sealed class DriveAuth {
    data class Authorized(val accessToken: String) : DriveAuth()
    data class NeedsConsent(val intentSender: IntentSender) : DriveAuth()
    data class Failed(val message: String) : DriveAuth()
}

/**
 * Minta access token ber-scope `drive.file` lewat Authorization API (Google Identity).
 * `drive.file` = hanya file yang dibuat app ini; scope non-sensitive → consent screen bisa
 * di-publish ke Production tanpa verifikasi Google (beda dengan `drive.appdata` yang sensitive).
 * - Sudah pernah diizinkan → token langsung (silent), termasuk dari WorkManager.
 * - Belum → kembalikan [DriveAuth.NeedsConsent]; UI meluncurkan PendingIntent, lalu [resultFromIntent].
 */
@Singleton
class DriveAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val driveFileScope = Scope("https://www.googleapis.com/auth/drive.file")

    private fun request() = AuthorizationRequest.builder()
        .setRequestedScopes(listOf(driveFileScope))
        .build()

    suspend fun authorize(): DriveAuth = try {
        val result = Identity.getAuthorizationClient(context).authorize(request()).await()
        if (result.hasResolution()) {
            DriveAuth.NeedsConsent(result.pendingIntent!!.intentSender)
        } else {
            result.accessToken?.let { DriveAuth.Authorized(it) }
                ?: DriveAuth.Failed("Token Drive kosong")
        }
    } catch (e: Exception) {
        DriveAuth.Failed(e.message ?: "Otorisasi Drive gagal")
    }

    /** Lanjutan setelah user menyetujui consent (hasil PendingIntent). */
    fun resultFromIntent(data: Intent?): DriveAuth = try {
        val result = Identity.getAuthorizationClient(context).getAuthorizationResultFromIntent(data)
        result.accessToken?.let { DriveAuth.Authorized(it) } ?: DriveAuth.Failed("Token Drive kosong")
    } catch (e: Exception) {
        DriveAuth.Failed(e.message ?: "Otorisasi Drive gagal")
    }

    /** Token tanpa UI (untuk WorkManager). null bila perlu consent / gagal. */
    suspend fun silentToken(): String? = (authorize() as? DriveAuth.Authorized)?.accessToken
}
