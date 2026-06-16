package com.taufik.managemoney.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.ai.AiConfig
import com.taufik.managemoney.data.ai.AiConfigStore
import com.taufik.managemoney.data.ai.AiConnectionTester
import com.taufik.managemoney.data.ai.AiProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Hasil "Tes parse" untuk ditampilkan (sukses hijau / gagal merah). */
data class TestOutcome(val ok: Boolean, val message: String)

data class AiSettingsUiState(
    val loaded: Boolean = false,
    val provider: AiProvider = AiProvider.GEMINI,
    val model: String = AiProvider.GEMINI.defaultModel,
    val baseUrl: String = "",
    val apiKey: String = "",
    val saved: Boolean = false,
    val testing: Boolean = false,
    val testOutcome: TestOutcome? = null,
) {
    val providers: List<AiProvider> = AiProvider.entries
    val showBaseUrl: Boolean get() = provider.openAiCompatible
    val canSave: Boolean get() = apiKey.isNotBlank() && (!provider.openAiCompatible || baseUrl.isNotBlank())
}

@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val configStore: AiConfigStore,
    private val tester: AiConnectionTester,
) : ViewModel() {

    private val _state = MutableStateFlow(AiSettingsUiState())
    val state: StateFlow<AiSettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val cfg = configStore.current()
            _state.update {
                it.copy(
                    loaded = true,
                    provider = cfg.provider,
                    model = cfg.model,
                    baseUrl = cfg.baseUrl,
                    apiKey = cfg.apiKey,
                )
            }
        }
    }

    /** Ganti provider → isi ulang model/baseUrl dengan default preset + key tersimpan provider itu. */
    fun selectProvider(provider: AiProvider) {
        viewModelScope.launch {
            val key = configStore.keyFor(provider)
            _state.update {
                it.copy(
                    provider = provider,
                    model = provider.defaultModel,
                    baseUrl = provider.defaultBaseUrl,
                    apiKey = key,
                    saved = false,
                    testOutcome = null,
                )
            }
        }
    }

    fun setModel(value: String) = _state.update { it.copy(model = value, saved = false, testOutcome = null) }
    fun setBaseUrl(value: String) = _state.update { it.copy(baseUrl = value, saved = false, testOutcome = null) }
    fun setApiKey(value: String) = _state.update { it.copy(apiKey = value, saved = false, testOutcome = null) }

    private fun formConfig() = AiConfig(
        provider = _state.value.provider,
        model = _state.value.model.trim().ifBlank { _state.value.provider.defaultModel },
        baseUrl = _state.value.baseUrl.trim(),
        apiKey = _state.value.apiKey.trim(),
    )

    fun save() {
        viewModelScope.launch {
            configStore.save(formConfig())
            _state.update { it.copy(saved = true) }
        }
    }

    fun test() {
        if (_state.value.testing) return
        viewModelScope.launch {
            _state.update { it.copy(testing = true, testOutcome = null) }
            val outcome = tester.test(formConfig()).fold(
                onSuccess = { list ->
                    TestOutcome(true, "Berhasil — ${list.size} transaksi terdeteksi dari kalimat uji")
                },
                onFailure = { e ->
                    TestOutcome(false, e.message ?: "Gagal terhubung")
                },
            )
            _state.update { it.copy(testing = false, testOutcome = outcome) }
        }
    }
}
