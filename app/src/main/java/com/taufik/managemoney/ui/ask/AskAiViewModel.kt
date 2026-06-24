package com.taufik.managemoney.ui.ask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.domain.ai.FinancialAnalyst
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Satu pesan chat (true = dari user, false = dari AI). */
data class ChatMsg(val fromUser: Boolean, val text: String)

/**
 * Tanya AI. Data tidak lagi dirakit di depan — [FinancialAnalyst.ask] memakai function calling,
 * jadi AI mengambil data sendiri (saldo/ringkasan/cari transaksi) sesuai pertanyaan.
 */
@HiltViewModel
class AskAiViewModel @Inject constructor(
    private val analyst: FinancialAnalyst,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMsg>>(emptyList())
    val messages: StateFlow<List<ChatMsg>> = _messages.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    fun send(question: String) {
        val q = question.trim()
        if (q.isBlank() || _sending.value) return
        _messages.value = _messages.value + ChatMsg(fromUser = true, text = q)
        viewModelScope.launch {
            _sending.value = true
            analyst.ask(q)
                .onSuccess { _messages.value = _messages.value + ChatMsg(false, it) }
                .onFailure { e ->
                    _messages.value = _messages.value + ChatMsg(false, "Maaf, gagal menjawab. ${e.message ?: ""}".trim())
                }
            _sending.value = false
        }
    }
}
