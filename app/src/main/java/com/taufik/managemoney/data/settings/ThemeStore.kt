package com.taufik.managemoney.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Mode tema yang dipilih pengguna. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Cara mewarnai pemasukan/pengeluaran.
 * - [FOLLOW_ACCENT]: masuk = warna aksen, keluar = netral (makna tetap dari tanda +/− & panah).
 * - [FIXED]: hijau-toska vs oranye (tervalidasi buta warna), tak ikut aksen.
 */
enum class FlowColorMode { FOLLOW_ACCENT, FIXED }

/** Preferensi tampilan lengkap. [accentArgb] = warna aksen ARGB (default biru). */
data class ThemePrefs(
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val accentArgb: Int = DEFAULT_ACCENT,
    val flowColor: FlowColorMode = FlowColorMode.FOLLOW_ACCENT,
) {
    companion object {
        const val DEFAULT_ACCENT: Int = 0xFF2F7BF6.toInt()
    }
}

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

/** Simpan & baca preferensi tampilan (DataStore). Default: ikuti sistem, aksen biru, masuk/keluar ikut aksen. */
@Singleton
class ThemeStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val modeKey = stringPreferencesKey("theme_mode")
    private val accentKey = intPreferencesKey("accent_argb")
    private val flowKey = stringPreferencesKey("flow_color")

    val prefs: Flow<ThemePrefs> = context.themeDataStore.data.map { p ->
        ThemePrefs(
            mode = p[modeKey]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            accentArgb = p[accentKey] ?: ThemePrefs.DEFAULT_ACCENT,
            flowColor = p[flowKey]?.let { runCatching { FlowColorMode.valueOf(it) }.getOrNull() }
                ?: FlowColorMode.FOLLOW_ACCENT,
        )
    }

    /** Kompatibilitas pemanggil lama. */
    val mode: Flow<ThemeMode> = prefs.map { it.mode }

    suspend fun set(mode: ThemeMode) {
        context.themeDataStore.edit { it[modeKey] = mode.name }
    }

    suspend fun setAccent(argb: Int) {
        context.themeDataStore.edit { it[accentKey] = argb or 0xFF000000.toInt() }
    }

    suspend fun setFlowColor(mode: FlowColorMode) {
        context.themeDataStore.edit { it[flowKey] = mode.name }
    }
}
