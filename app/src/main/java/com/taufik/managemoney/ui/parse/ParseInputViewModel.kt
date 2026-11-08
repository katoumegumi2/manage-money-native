package com.taufik.managemoney.ui.parse

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.repo.ParseRepository
import com.taufik.managemoney.domain.ai.ImageTransactionParser
import com.taufik.managemoney.domain.model.ParsedTransaction
import com.taufik.managemoney.ui.share.ImageReader
import com.taufik.managemoney.ui.navigation.ConfirmPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ParseInputUiState(
    val text: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val emptyResult: Boolean = false,
    val payload: String? = null,
) {
    val canProcess: Boolean get() = text.isNotBlank() && !loading
}

@HiltViewModel
class ParseInputViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parseRepository: ParseRepository,
    private val imageParser: ImageTransactionParser,
) : ViewModel() {

    private val _state = MutableStateFlow(ParseInputUiState())
    val state: StateFlow<ParseInputUiState> = _state.asStateFlow()

    fun setText(value: String) =
        _state.update { it.copy(text = value, error = null, emptyResult = false) }

    fun process() {
        val text = _state.value.text.trim()
        if (text.isEmpty() || _state.value.loading) return
        launchParse { parseRepository.parse(text) }
    }

    /** Foto struk/bukti bayar yang dipilih manual dari galeri → AI vision → Konfirmasi. */
    fun processImages(uris: List<Uri>) {
        if (uris.isEmpty() || _state.value.loading) return
        launchParse {
            val images = withContext(Dispatchers.IO) { uris.mapNotNull { ImageReader.read(context, it) } }
            if (images.isEmpty()) Result.failure(IllegalStateException("Gambar tidak bisa dibaca"))
            else imageParser.parse(images)
        }
    }

    private fun launchParse(block: suspend () -> Result<List<ParsedTransaction>>) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, emptyResult = false) }
            block()
                .onSuccess { list ->
                    if (list.isEmpty()) {
                        _state.update { it.copy(loading = false, emptyResult = true) }
                    } else {
                        _state.update { it.copy(loading = false, payload = ConfirmPayload.encode(list)) }
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, error = e.message ?: "Gagal memproses") }
                }
        }
    }

    /** Setelah navigasi ke Konfirmasi, kosongkan payload agar tidak terpicu ulang. */
    fun consumePayload() = _state.update { it.copy(payload = null) }
}
