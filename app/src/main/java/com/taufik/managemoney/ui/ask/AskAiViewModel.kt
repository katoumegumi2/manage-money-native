package com.taufik.managemoney.ui.ask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.domain.ai.FinancialAnalyst
import com.taufik.managemoney.domain.model.AskAccount
import com.taufik.managemoney.domain.model.AskContext
import com.taufik.managemoney.domain.model.CategoryAgg
import com.taufik.managemoney.domain.usecase.ObserveBalances
import com.taufik.managemoney.domain.usecase.ObserveExpenseByCategory
import com.taufik.managemoney.domain.usecase.ObserveMonthlyFlow
import com.taufik.managemoney.domain.usecase.ObserveMonthlyTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

private val localeId = Locale.forLanguageTag("id-ID")
private val monthLabelFormat = DateTimeFormatter.ofPattern("MMMM yyyy", localeId)

/** Satu pesan chat (true = dari user, false = dari AI). */
data class ChatMsg(val fromUser: Boolean, val text: String)

@HiltViewModel
class AskAiViewModel @Inject constructor(
    observeExpenseByCategory: ObserveExpenseByCategory,
    observeMonthlyFlow: ObserveMonthlyFlow,
    observeMonthlyTrend: ObserveMonthlyTrend,
    observeBalances: ObserveBalances,
    private val analyst: FinancialAnalyst,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Jakarta"))
    private val periodLabel = today.format(monthLabelFormat).replaceFirstChar { it.titlecase(localeId) }

    private val context: StateFlow<AskContext?> = combine(
        observeExpenseByCategory(today),
        observeMonthlyFlow(today),
        observeMonthlyTrend(today),
        observeBalances(),
    ) { recap, monthly, trend, balances ->
        val prev = trend.getOrNull(trend.size - 2)
        AskContext(
            today = today.toString(),
            period = periodLabel,
            masuk = monthly.masuk,
            keluar = monthly.keluar,
            net = monthly.masuk - monthly.keluar,
            prevMasuk = prev?.masuk ?: 0L,
            prevKeluar = prev?.keluar ?: 0L,
            categories = recap.map { CategoryAgg(it.kategori, it.total) },
            accounts = balances.map { AskAccount(it.nama, it.saldo) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _messages = MutableStateFlow<List<ChatMsg>>(emptyList())
    val messages: StateFlow<List<ChatMsg>> = _messages.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    fun send(question: String) {
        val q = question.trim()
        if (q.isBlank() || _sending.value) return
        val ctx = context.value
        _messages.value = _messages.value + ChatMsg(fromUser = true, text = q)
        if (ctx == null) {
            _messages.value = _messages.value + ChatMsg(false, "Data belum siap, coba sebentar lagi.")
            return
        }
        viewModelScope.launch {
            _sending.value = true
            analyst.ask(q, ctx)
                .onSuccess { _messages.value = _messages.value + ChatMsg(false, it) }
                .onFailure { e ->
                    _messages.value = _messages.value + ChatMsg(false, "Maaf, gagal menjawab. ${e.message ?: ""}".trim())
                }
            _sending.value = false
        }
    }
}
