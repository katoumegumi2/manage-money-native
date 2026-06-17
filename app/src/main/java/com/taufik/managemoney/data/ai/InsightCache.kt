package com.taufik.managemoney.data.ai

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.insightDataStore by preferencesDataStore(name = "insight_cache")

/**
 * Cache insight AI per periode. Insight di-regenerate hanya bila [signature] periode berubah
 * (angka berubah) → hemat call LLM untuk bulan yang sudah lewat.
 */
@Singleton
class InsightCache @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private fun textKey(periodKey: String) = stringPreferencesKey("insight_$periodKey")
    private fun sigKey(periodKey: String) = stringPreferencesKey("sig_$periodKey")

    /** Insight tersimpan bila signature cocok; null bila belum ada / data sudah berubah. */
    suspend fun get(periodKey: String, signature: String): String? {
        val prefs = context.insightDataStore.data.first()
        return if (prefs[sigKey(periodKey)] == signature) prefs[textKey(periodKey)] else null
    }

    suspend fun put(periodKey: String, signature: String, text: String) {
        context.insightDataStore.edit {
            it[textKey(periodKey)] = text
            it[sigKey(periodKey)] = signature
        }
    }
}
