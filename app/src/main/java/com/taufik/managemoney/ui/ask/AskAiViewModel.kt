package com.taufik.managemoney.ui.ask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.ai.AiConfigStore
import com.taufik.managemoney.data.ai.InsightCache
import com.taufik.managemoney.domain.ai.FinancialAnalyst
import com.taufik.managemoney.domain.model.AggregatedFinance
import com.taufik.managemoney.domain.model.CategoryAgg
import com.taufik.managemoney.domain.usecase.ObserveExpenseByCategory
import com.taufik.managemoney.domain.usecase.ObserveMonthlyFlow
import com.taufik.managemoney.domain.usecase.ObserveMonthlyTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/** Satu pesan chat (true = dari user, false = dari AI). */
data class ChatMsg(val fromUser: Boolean, val text: String)

/** Status kartu "Insight bulan ini" di layar Asisten AI. */
data class AskInsight(
    val text: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val canGenerate: Boolean = false,
)

private val localeId = Locale.forLanguageTag("id-ID")
private val monthLabelFormat = DateTimeFormatter.ofPattern("MMMM yyyy", localeId)

/**
 * Asisten AI: chat tanya-jawab ([FinancialAnalyst.ask], function calling) + kartu insight bulanan
 * (rekap naratif yang di-cache, sama seperti Laporan sebelumnya — kini rumahnya di sini).
 */
@HiltViewModel
class AskAiViewModel @Inject constructor(
    private val analyst: FinancialAnalyst,
    configStore: AiConfigStore,
    observeExpenseByCategory: ObserveExpenseByCategory,
    observeMonthlyFlow: ObserveMonthlyFlow,
    observeMonthlyTrend: ObserveMonthlyTrend,
    private val insightCache: InsightCache,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Jakarta"))
    private val periodKey: String = today.toString().substring(0, 7)
    private val periodLabel: String = today.format(monthLabelFormat)
        .replaceFirstChar { it.titlecase(localeId) }

    /** Nama provider AI aktif (untuk kartu status), mis. "DeepSeek". */
    val provider: StateFlow<String> = configStore.config
        .map { it.provider.displayName }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val aggregate: StateFlow<AggregatedFinance?> = combine(
        observeExpenseByCategory(today),
        observeMonthlyFlow(today),
        observeMonthlyTrend(today),
    ) { recap, monthly, trend ->
        val prev = trend.getOrNull(trend.size - 2)
        AggregatedFinance(
            period = periodLabel,
            periodKey = periodKey,
            masuk = monthly.masuk,
            keluar = monthly.keluar,
            net = monthly.masuk - monthly.keluar,
            categories = recap.map { CategoryAgg(it.kategori, it.total) },
            prevMasuk = prev?.masuk ?: 0L,
            prevKeluar = prev?.keluar ?: 0L,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _insight = MutableStateFlow(AskInsight())
    val insight: StateFlow<AskInsight> = _insight.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMsg>>(emptyList())
    val messages: StateFlow<List<ChatMsg>> = _messages.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    init {
        viewModelScope.launch {
            aggregate.distinctUntilChanged { a, b -> a?.signature == b?.signature }.collect { agg ->
                if (agg == null) return@collect
                val cached = insightCache.get(agg.periodKey, agg.signature)
                _insight.value = when {
                    cached != null -> AskInsight(text = cached)
                    agg.isEmpty -> AskInsight(canGenerate = false)
                    else -> AskInsight(canGenerate = true)
                }
            }
        }
    }

    fun generateInsight() {
        val agg = aggregate.value ?: return
        if (_insight.value.loading || agg.isEmpty) return
        viewModelScope.launch {
            _insight.value = _insight.value.copy(loading = true, error = null)
            analyst.summarize(agg)
                .onSuccess { text ->
                    insightCache.put(agg.periodKey, agg.signature, text)
                    _insight.value = AskInsight(text = text)
                }
                .onFailure { e ->
                    _insight.value = AskInsight(canGenerate = true, error = e.message ?: "Gagal membuat insight")
                }
        }
    }

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
