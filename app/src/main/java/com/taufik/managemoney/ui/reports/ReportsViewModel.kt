package com.taufik.managemoney.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.ai.InsightCache
import com.taufik.managemoney.domain.ai.FinancialAnalyst
import com.taufik.managemoney.domain.model.AggregatedFinance
import com.taufik.managemoney.domain.model.CategoryAgg
import com.taufik.managemoney.domain.model.CategorySum
import com.taufik.managemoney.domain.usecase.MonthlyPoint
import com.taufik.managemoney.domain.usecase.ObserveExpenseByCategory
import com.taufik.managemoney.domain.usecase.ObserveMonthlyFlow
import com.taufik.managemoney.domain.usecase.ObserveMonthlyTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

private val localeId = Locale.forLanguageTag("id-ID")
private val monthLabelFormat = DateTimeFormatter.ofPattern("MMMM yyyy", localeId)

data class CategoryBar(val label: String, val total: Long, val fraction: Float)

data class TrendBar(
    val label: String,
    val masuk: Long,
    val keluar: Long,
    val masukFraction: Float,
    val keluarFraction: Float,
)

/** Status insight AI (rekap naratif bulan ini). */
data class InsightUi(
    val text: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val canGenerate: Boolean = false,
)

data class ReportsUiState(
    val loading: Boolean = true,
    val period: String = "",
    val masuk: Long = 0,
    val keluar: Long = 0,
    val net: Long = 0,
    val keluarDeltaPct: Int? = null, // perubahan pengeluaran vs bulan lalu (%), null bila tak ada acuan
    val bars: List<CategoryBar> = emptyList(),
    val trend: List<TrendBar> = emptyList(),
    val insight: InsightUi = InsightUi(),
    val selectedPeriod: com.taufik.managemoney.domain.usecase.ReportPeriod = com.taufik.managemoney.domain.usecase.ReportPeriod.MONTH,
    val report: com.taufik.managemoney.domain.usecase.PeriodReport? = null,
) {
    val isEmpty: Boolean get() = masuk == 0L && keluar == 0L && bars.isEmpty()
    val hasTrend: Boolean get() = trend.any { it.masuk > 0 || it.keluar > 0 }
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    observeExpenseByCategory: ObserveExpenseByCategory,
    observeMonthlyFlow: ObserveMonthlyFlow,
    observeMonthlyTrend: ObserveMonthlyTrend,
    observePeriodReport: com.taufik.managemoney.domain.usecase.ObservePeriodReport,
    private val analyst: FinancialAnalyst,
    private val insightCache: InsightCache,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Jakarta"))
    private val periodKey: String = today.toString().substring(0, 7) // YYYY-MM
    private val periodLabel: String = today.format(monthLabelFormat)
        .replaceFirstChar { it.titlecase(localeId) }

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

    private val insightState = kotlinx.coroutines.flow.MutableStateFlow(InsightUi())

    private val periodState = kotlinx.coroutines.flow.MutableStateFlow(com.taufik.managemoney.domain.usecase.ReportPeriod.MONTH)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val periodReport = periodState.flatMapLatest { observePeriodReport(it, today) }

    fun setPeriod(p: com.taufik.managemoney.domain.usecase.ReportPeriod) { periodState.value = p }

    private val baseState = combine(
        aggregate,
        observeExpenseByCategory(today),
        observeMonthlyTrend(today),
        insightState,
    ) { agg, recap, trend, insight ->
        if (agg == null) {
            ReportsUiState(loading = true)
        } else {
            ReportsUiState(
                loading = false,
                period = agg.period,
                masuk = agg.masuk,
                keluar = agg.keluar,
                net = agg.net,
                keluarDeltaPct = deltaPct(agg.keluar, agg.prevKeluar),
                bars = toBars(recap),
                trend = toTrend(trend),
                insight = insight,
            )
        }
    }

    val state: StateFlow<ReportsUiState> = combine(baseState, periodState, periodReport) { base, p, r ->
        base.copy(selectedPeriod = p, report = r)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())

    init {
        // Muat insight dari cache tiap kali angka periode berubah; bila belum ada → tawarkan generate.
        viewModelScope.launch {
            aggregate.distinctUntilChanged { a, b -> a?.signature == b?.signature }.collect { agg ->
                if (agg == null) return@collect
                val cached = insightCache.get(agg.periodKey, agg.signature)
                insightState.value = when {
                    cached != null -> InsightUi(text = cached)
                    agg.isEmpty -> InsightUi(canGenerate = false)
                    else -> InsightUi(canGenerate = true)
                }
            }
        }
    }

    fun generateInsight() {
        val agg = aggregate.value ?: return
        if (insightState.value.loading || agg.isEmpty) return
        viewModelScope.launch {
            insightState.value = insightState.value.copy(loading = true, error = null)
            analyst.summarize(agg)
                .onSuccess { text ->
                    insightCache.put(agg.periodKey, agg.signature, text)
                    insightState.value = InsightUi(text = text)
                }
                .onFailure { e ->
                    insightState.value = InsightUi(
                        canGenerate = true,
                        error = e.message ?: "Gagal membuat insight",
                    )
                }
        }
    }

    private fun deltaPct(now: Long, prev: Long): Int? =
        if (prev <= 0L) null else (((now - prev).toDouble() / prev) * 100).toInt()

    private fun toTrend(points: List<MonthlyPoint>): List<TrendBar> {
        val max = points.flatMap { listOf(it.masuk, it.keluar) }.maxOrNull()?.takeIf { it > 0 } ?: 0L
        return points.map { p ->
            TrendBar(
                label = p.label,
                masuk = p.masuk,
                keluar = p.keluar,
                masukFraction = if (max == 0L) 0f else (p.masuk.toFloat() / max).coerceIn(0f, 1f),
                keluarFraction = if (max == 0L) 0f else (p.keluar.toFloat() / max).coerceIn(0f, 1f),
            )
        }
    }

    private fun toBars(rows: List<CategorySum>): List<CategoryBar> {
        val max = rows.maxOfOrNull { it.total }?.takeIf { it > 0 } ?: return emptyList()
        return rows.map {
            CategoryBar(
                label = it.kategori.replaceFirstChar { c -> c.titlecase(localeId) },
                total = it.total,
                fraction = (it.total.toFloat() / max).coerceIn(0f, 1f),
            )
        }
    }
}
