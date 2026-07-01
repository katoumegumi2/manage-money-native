package com.taufik.managemoney.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.homeOrderDataStore by preferencesDataStore(name = "home_account_order")

/**
 * Urutan tampil kartu akun di Beranda (hasil drag-reorder), disimpan sebagai daftar id akun.
 * Terpisah dari `urutan` DB agar Beranda bisa diatur tanpa mengubah urutan di Kelola akun.
 */
@Singleton
class HomeAccountOrderStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val key = stringPreferencesKey("order_ids")

    val order: Flow<List<String>> = context.homeOrderDataStore.data.map { prefs ->
        prefs[key]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun save(ids: List<String>) {
        context.homeOrderDataStore.edit { it[key] = ids.joinToString(",") }
    }
}
