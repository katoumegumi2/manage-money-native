package com.taufik.managemoney.data.stt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Alasan kegagalan STT yang dipetakan ke pesan pengguna (lihat string `stt_error_*`). */
enum class SttError { NO_MATCH, NO_SPEECH, NETWORK, AUDIO, PERMISSION, BUSY, SERVER, UNAVAILABLE, OTHER }

/** Peristiwa dari `SpeechRecognizer`. Transkrip live lewat [Partial], hasil akhir lewat [Result]. */
sealed interface SttEvent {
    data object Ready : SttEvent
    data object BeginSpeech : SttEvent
    data object EndSpeech : SttEvent
    data class Partial(val text: String) : SttEvent
    data class Result(val text: String) : SttEvent
    data class Failure(val error: SttError) : SttEvent
}

/**
 * Pembungkus tipis Android `SpeechRecognizer`. Dipakai layer ui/voice; tidak menyentuh AI/parsing.
 * `listen()` adalah Flow dingin: mulai mendengarkan saat dikoleksi, lepas resource saat dibatalkan.
 * WAJIB dikoleksi di main thread (SpeechRecognizer harus dibuat & dipakai di main thread).
 */
@Singleton
class SpeechRecognizerManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun listen(localeTag: String = "id-ID"): Flow<SttEvent> = callbackFlow {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            trySend(SttEvent.Failure(SttError.UNAVAILABLE))
            close()
            return@callbackFlow
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { trySend(SttEvent.Ready) }
            override fun onBeginningOfSpeech() { trySend(SttEvent.BeginSpeech) }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { trySend(SttEvent.EndSpeech) }

            override fun onPartialResults(partialResults: Bundle?) {
                firstText(partialResults)?.let { trySend(SttEvent.Partial(it)) }
            }

            override fun onResults(results: Bundle?) {
                val text = firstText(results)
                if (text.isNullOrBlank()) {
                    trySend(SttEvent.Failure(SttError.NO_MATCH))
                } else {
                    trySend(SttEvent.Result(text))
                }
                close()
            }

            override fun onError(error: Int) {
                trySend(SttEvent.Failure(mapError(error)))
                close()
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        recognizer.setRecognitionListener(listener)
        recognizer.startListening(buildIntent(localeTag))

        awaitClose {
            recognizer.stopListening()
            recognizer.cancel()
            recognizer.destroy()
        }
    }

    private fun buildIntent(localeTag: String) =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            // Beri ruang kalimat panjang & jeda: jangan terlalu cepat memotong.
            // (Extra ini bersifat saran; sebagian recognizer Google mengabaikannya.)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 8_000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3_000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2_500)
        }

    private fun firstText(bundle: Bundle?): String? =
        bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

    private fun mapError(code: Int): SttError = when (code) {
        SpeechRecognizer.ERROR_NO_MATCH -> SttError.NO_MATCH
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SttError.NO_SPEECH
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> SttError.NETWORK
        SpeechRecognizer.ERROR_AUDIO -> SttError.AUDIO
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SttError.PERMISSION
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> SttError.BUSY
        SpeechRecognizer.ERROR_SERVER -> SttError.SERVER
        else -> SttError.OTHER
    }
}
