package com.taufik.managemoney.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.taufik.managemoney.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Akun Google hasil sign-in (autentikasi). idToken dipakai server bila perlu; di sini cukup identitas. */
data class GoogleAccount(val email: String, val name: String?, val idToken: String)

/** Gagal login yang bisa dibedakan UI (batal vs tak ada akun vs konfigurasi). */
sealed class SignInError(message: String) : Exception(message) {
    object Cancelled : SignInError("Login dibatalkan")
    object NoAccount : SignInError("Tidak ada akun Google di perangkat")
    object NotConfigured : SignInError("Login Google belum dikonfigurasi")
    class Unknown(message: String) : SignInError(message)
}

/**
 * Sign-in Google via Credential Manager (autentikasi). `serverClientId` = Web OAuth client
 * dari BuildConfig (local.properties). Otorisasi Drive (access token) ditangani terpisah di Phase 8c.
 */
@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private val webClientId: String = BuildConfig.GOOGLE_WEB_CLIENT_ID

    val isConfigured: Boolean get() = webClientId.isNotBlank()

    /**
     * Tampilkan pemilih akun Google & kembalikan akun terpilih.
     * [activityContext] HARUS context Activity (Credential Manager menampilkan UI).
     */
    suspend fun signIn(activityContext: Context): Result<GoogleAccount> {
        if (!isConfigured) return Result.failure(SignInError.NotConfigured)

        val option = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false) // tampilkan semua akun (login pertama)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        return try {
            val response = CredentialManager.create(appContext).getCredential(activityContext, request)
            val cred = response.credential
            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val gid = GoogleIdTokenCredential.createFrom(cred.data)
                Result.success(GoogleAccount(email = gid.id, name = gid.displayName, idToken = gid.idToken))
            } else {
                Result.failure(SignInError.Unknown("Tipe kredensial tidak dikenal"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(SignInError.Cancelled)
        } catch (e: NoCredentialException) {
            Result.failure(SignInError.NoAccount)
        } catch (e: GetCredentialException) {
            Result.failure(SignInError.Unknown(e.message ?: "Login gagal"))
        }
    }
}
