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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RESTART_DELAY_MS = 250L

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

    /** Teks dari segmen-segmen yang sudah final dalam sesi ini. */
    private var committed = ""

    /** Partial terakhir segmen berjalan (transkrip live). Kadang lebih lengkap dari hasil final. */
    private var lastPartial = ""

    private var startedAt = 0L
    private var lastSpeechAt = 0L
    private var finished = false

    fun startListening() {
        listenJob?.cancel()
        committed = ""
        lastPartial = ""
        finished = false
        startedAt = System.currentTimeMillis()
        lastSpeechAt = startedAt
        _state.value = VoiceUiState(phase = VoicePhase.LISTENING)
        listenSegment(restart = false)
    }

    /** Satu putaran SpeechRecognizer; saat selesai, lanjut putaran berikut bila user masih bicara. */
    private fun listenSegment(restart: Boolean = true) {
        listenJob = viewModelScope.launch {
            // Beri waktu recognizer lama dilepas (destroy) agar tak kena ERROR_RECOGNIZER_BUSY.
            if (restart) delay(RESTART_DELAY_MS)
            stt.listen().collect { event ->
                if (finished) return@collect
                when (event) {
                    SttEvent.Ready, SttEvent.EndSpeech -> Unit
                    SttEvent.BeginSpeech -> lastSpeechAt = System.currentTimeMillis()
                    is SttEvent.Partial -> {
                        if (event.text.isNotBlank()) {
                            lastPartial = event.text
                            lastSpeechAt = System.currentTimeMillis()
                        }
                        _state.update { it.copy(partial = VoiceSession.join(committed, event.text)) }
                    }
                    is SttEvent.Result -> {
                        commit(VoiceSession.best(event.text, lastPartial))
                        onSegmentEnd()
                    }
                    is SttEvent.Failure -> when (event.error) {
                        // Segmen berakhir tanpa hasil final (biasanya karena hening) → simpan partial, lanjut/selesai.
                        SttError.NO_MATCH, SttError.NO_SPEECH -> {
                            commit(lastPartial)
                            onSegmentEnd(noSpeechError = event.error)
                        }
                        else -> if (committed.isNotBlank() || lastPartial.isNotBlank()) {
                            commit(lastPartial)
                            finish()
                        } else {
                            finished = true
                            _state.update { it.copy(phase = VoicePhase.IDLE, sttError = event.error) }
                        }
                    }
                }
            }
        }
    }

    private fun commit(segment: String) {
        committed = VoiceSession.join(committed, segment)
        lastPartial = ""
        _state.update { it.copy(partial = committed) }
    }

    private fun onSegmentEnd(noSpeechError: SttError = SttError.NO_SPEECH) {
        val now = System.currentTimeMillis()
        if (VoiceSession.shouldContinue(silentMs = now - lastSpeechAt, totalMs = now - startedAt)) {
            listenSegment() // mulai ulang: jeda sebentar tidak memotong ucapan
        } else {
            finish(noSpeechError)
        }
    }

    /** Tombol "Selesai": hentikan mic sekarang dan proses yang sudah terucap. */
    fun finishListening() {
        if (finished) return
        listenJob?.cancel()
        commit(lastPartial)
        finish()
    }

    private fun finish(emptyError: SttError = SttError.NO_SPEECH) {
        if (finished) return
        finished = true
        val text = committed.trim()
        if (text.isBlank()) {
            _state.update { it.copy(phase = VoicePhase.IDLE, sttError = emptyError) }
            return
        }
        _state.update { it.copy(finalText = text, partial = text) }
        viewModelScope.launch { parse(text) }
    }

    /** Berhenti mendengarkan (mis. tombol Batal) tanpa memproses. */
    fun cancelListening() {
        finished = true
        listenJob?.cancel()
        listenJob = null
        _state.update { it.copy(phase = VoicePhase.IDLE) }
    }

    /** Dipanggil bila izin mikrofon ditolak. */
    fun onPermissionDenied() {
        finished = true
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
