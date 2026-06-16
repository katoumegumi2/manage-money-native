package com.taufik.managemoney.ui.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.repo.ParseRepository
import com.taufik.managemoney.data.stt.SpeechRecognizerManager
import com.taufik.managemoney.data.stt.SttError
import com.taufik.managemoney.data.stt.SttEvent
import com.taufik.managemoney.ui.navigation.ConfirmPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class VoicePhase { IDLE, LISTENING, PROCESSING }

data class VoiceUiState(
    val phase: VoicePhase = VoicePhase.IDLE,
    val partial: String = "",
    val finalText: String = "",
    /** Kegagalan STT (null bila tak ada). Dipetakan ke pesan di layar. */
    val sttError: SttError? = null,
    /** Kegagalan parsing AI (pesan sudah dalam Bahasa Indonesia dari exception). */
    val parseError: String? = null,
    val emptyResult: Boolean = false,
    val payload: String? = null,
)

@HiltViewModel
class VoiceInputViewModel @Inject constructor(
    private val stt: SpeechRecognizerManager,
    private val parseRepository: ParseRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(VoiceUiState())
    val state: StateFlow<VoiceUiState> = _state.asStateFlow()

    private var listenJob: Job? = null

    /** Partial terakhir (transkrip live). Kadang lebih lengkap dari hasil final → dipakai sebagai fallback. */
    private var lastPartial = ""

    fun startListening() {
        listenJob?.cancel()
        lastPartial = ""
        _state.value = VoiceUiState(phase = VoicePhase.LISTENING)
        listenJob = viewModelScope.launch {
            stt.listen().collect { event ->
                when (event) {
                    SttEvent.Ready, SttEvent.BeginSpeech, SttEvent.EndSpeech -> Unit
                    is SttEvent.Partial -> {
                        if (event.text.isNotBlank()) lastPartial = event.text
                        _state.update { it.copy(partial = event.text) }
                    }
                    is SttEvent.Result -> {
                        // Final kadang lebih buruk/pendek dari partial terakhir → pilih yang lebih lengkap.
                        val best = if (event.text.length >= lastPartial.length) event.text else lastPartial
                        _state.update { it.copy(finalText = best, partial = best) }
                        parse(best)
                    }
                    is SttEvent.Failure -> {
                        // Final kosong tapi sempat ada transkrip live → pakai itu, jangan buang.
                        if ((event.error == SttError.NO_MATCH || event.error == SttError.NO_SPEECH) && lastPartial.isNotBlank()) {
                            _state.update { it.copy(finalText = lastPartial, partial = lastPartial) }
                            parse(lastPartial)
                        } else {
                            _state.update { it.copy(phase = VoicePhase.IDLE, sttError = event.error) }
                        }
                    }
                }
            }
        }
    }

    /** Berhenti mendengarkan (mis. tombol Batal) tanpa memproses. */
    fun cancelListening() {
        listenJob?.cancel()
        listenJob = null
        _state.update { it.copy(phase = VoicePhase.IDLE) }
    }

    /** Dipanggil bila izin mikrofon ditolak. */
    fun onPermissionDenied() {
        listenJob?.cancel()
        _state.value = VoiceUiState(phase = VoicePhase.IDLE, sttError = SttError.PERMISSION)
    }

    private suspend fun parse(text: String) {
        _state.update { it.copy(phase = VoicePhase.PROCESSING, sttError = null, parseError = null) }
        parseRepository.parse(text)
            .onSuccess { list ->
                if (list.isEmpty()) {
                    _state.update { it.copy(phase = VoicePhase.IDLE, emptyResult = true) }
                } else {
                    _state.update {
                        it.copy(phase = VoicePhase.IDLE, payload = ConfirmPayload.encode(list))
                    }
                }
            }
            .onFailure { e ->
                _state.update {
                    it.copy(phase = VoicePhase.IDLE, parseError = e.message ?: "Gagal memproses")
                }
            }
    }

    /** Setelah navigasi ke Konfirmasi, kosongkan payload agar tidak terpicu ulang. */
    fun consumePayload() = _state.update { it.copy(payload = null) }

    override fun onCleared() {
        listenJob?.cancel()
    }
}
