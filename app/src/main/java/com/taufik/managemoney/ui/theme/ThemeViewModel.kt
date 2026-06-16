package com.taufik.managemoney.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.settings.ThemeMode
import com.taufik.managemoney.data.settings.ThemeStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeStore: ThemeStore,
) : ViewModel() {
    val mode: StateFlow<ThemeMode> = themeStore.mode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeMode.SYSTEM,
    )

    fun set(mode: ThemeMode) {
        viewModelScope.launch { themeStore.set(mode) }
    }
}
