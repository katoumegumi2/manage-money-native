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

/** Mode tema yang dipilih pengguna. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

/** Simpan & baca preferensi tema (DataStore). Default mengikuti sistem. */
@Singleton
class ThemeStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val key = stringPreferencesKey("theme_mode")

    val mode: Flow<ThemeMode> = context.themeDataStore.data.map { prefs ->
        prefs[key]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
    }

    suspend fun set(mode: ThemeMode) {
        context.themeDataStore.edit { it[key] = mode.name }
    }
}
