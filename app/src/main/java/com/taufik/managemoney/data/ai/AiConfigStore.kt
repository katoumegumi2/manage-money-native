package com.taufik.managemoney.data.ai

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.taufik.managemoney.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.aiDataStore by preferencesDataStore(name = "ai_config")

/**
 * Penyimpanan konfigurasi AI: field non-rahasia (provider/model/baseUrl) di DataStore,
 * **API key terenkripsi** di EncryptedSharedPreferences (key per-provider agar tak hilang saat tukar).
 * Key TIDAK pernah ter-log / masuk VCS (CLAUDE.md).
 */
@Singleton
class AiConfigStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val PROVIDER = stringPreferencesKey("provider")
        val MODEL = stringPreferencesKey("model")
        val BASE_URL = stringPreferencesKey("base_url")
    }

    // Lazy: pembuatan EncryptedSharedPreferences melakukan kripto (hindari di main thread).
    private val securePrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "ai_secrets",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    val config: Flow<AiConfig> = context.aiDataStore.data.map { prefs ->
        val provider = prefs[Keys.PROVIDER]?.let { runCatching { AiProvider.valueOf(it) }.getOrNull() }
            ?: AiProvider.GEMINI
        AiConfig(
            provider = provider,
            model = prefs[Keys.MODEL]?.ifBlank { provider.defaultModel } ?: provider.defaultModel,
            baseUrl = prefs[Keys.BASE_URL] ?: provider.defaultBaseUrl,
            apiKey = readKey(provider),
        )
    }

    suspend fun current(): AiConfig = config.first()

    /** Key tersimpan untuk sebuah provider; fallback `BuildConfig.GEMINI_API_KEY` bila Gemini & belum diatur. */
    suspend fun keyFor(provider: AiProvider): String = withContext(Dispatchers.IO) { readKey(provider) }

    private fun readKey(provider: AiProvider): String {
        val stored = securePrefs.getString(secretKey(provider), "").orEmpty()
        return when {
            stored.isNotBlank() -> stored
            provider == AiProvider.GEMINI -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }
    }

    suspend fun save(cfg: AiConfig) = withContext(Dispatchers.IO) {
        securePrefs.edit().putString(secretKey(cfg.provider), cfg.apiKey).apply()
        context.aiDataStore.edit { prefs ->
            prefs[Keys.PROVIDER] = cfg.provider.name
            prefs[Keys.MODEL] = cfg.model
            prefs[Keys.BASE_URL] = cfg.baseUrl
        }
    }

    private fun secretKey(provider: AiProvider) = "api_key_${provider.name}"
}
