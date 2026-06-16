package com.taufik.managemoney.data.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Status onboarding & akun Google tertaut (untuk gating Welcome + tampil di Pengaturan). */
data class AuthState(
    val onboarded: Boolean = false,
    val skipBackup: Boolean = false,
    val email: String? = null,
    val name: String? = null,
) {
    val signedIn: Boolean get() = email != null
}

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthStateStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val onboardedKey = booleanPreferencesKey("onboarded")
    private val skipBackupKey = booleanPreferencesKey("skip_backup")
    private val emailKey = stringPreferencesKey("account_email")
    private val nameKey = stringPreferencesKey("account_name")

    val state: Flow<AuthState> = context.authDataStore.data.map { p ->
        AuthState(
            onboarded = p[onboardedKey] ?: false,
            skipBackup = p[skipBackupKey] ?: false,
            email = p[emailKey],
            name = p[nameKey],
        )
    }

    /** Tandai akun Google tertaut (login sukses). Sekaligus selesai onboarding. */
    suspend fun setSignedIn(email: String, name: String?) {
        context.authDataStore.edit {
            it[emailKey] = email
            if (name != null) it[nameKey] = name else it.remove(nameKey)
            it[onboardedKey] = true
            it[skipBackupKey] = false
        }
    }

    /** Pilih "Pakai tanpa backup" — selesai onboarding tanpa akun. */
    suspend fun setSkipBackup() {
        context.authDataStore.edit {
            it[skipBackupKey] = true
            it[onboardedKey] = true
            it.remove(emailKey)
            it.remove(nameKey)
        }
    }

    /** Keluar akun — hapus tautan, tetap onboarded (kembali ke mode tanpa backup). */
    suspend fun clearAccount() {
        context.authDataStore.edit {
            it.remove(emailKey)
            it.remove(nameKey)
            it[skipBackupKey] = true
        }
    }
}
