package com.taufik.managemoney.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.settings.FlowColorMode
import com.taufik.managemoney.data.settings.ThemeMode
import com.taufik.managemoney.data.settings.ThemePrefs
import com.taufik.managemoney.data.settings.ThemeStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeStore: ThemeStore,
) : ViewModel() {
    val prefs: StateFlow<ThemePrefs> = themeStore.prefs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemePrefs(),
    )

    val mode: StateFlow<ThemeMode> = themeStore.prefs.map { it.mode }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeMode.SYSTEM,
    )

    fun set(mode: ThemeMode) {
        viewModelScope.launch { themeStore.set(mode) }
    }

    fun setAccent(argb: Int) {
        viewModelScope.launch { themeStore.setAccent(argb) }
    }

    fun setFlowColor(mode: FlowColorMode) {
        viewModelScope.launch { themeStore.setFlowColor(mode) }
    }
}
