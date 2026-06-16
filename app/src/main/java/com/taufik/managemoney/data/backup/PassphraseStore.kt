package com.taufik.managemoney.data.backup

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simpan passphrase backup di EncryptedSharedPreferences (Android Keystore-backed).
 * Tidak pernah diunggah / di-log. Device-bound → di HP baru user WAJIB ketik ulang saat restore.
 */
@Singleton
class PassphraseStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "backup_secret",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private val key = "passphrase"

    fun has(): Boolean = !prefs.getString(key, null).isNullOrEmpty()

    fun get(): CharArray? = prefs.getString(key, null)?.toCharArray()

    fun set(passphrase: String) {
        prefs.edit().putString(key, passphrase).apply()
    }

    fun clear() {
        prefs.edit().remove(key).apply()
    }
}
