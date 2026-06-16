package com.taufik.managemoney.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.domain.model.CategorySum
import com.taufik.managemoney.domain.usecase.MonthlyPoint
import com.taufik.managemoney.domain.usecase.ObserveExpenseByCategory
import com.taufik.managemoney.domain.usecase.ObserveMonthlyFlow
import com.taufik.managemoney.domain.usecase.ObserveMonthlyTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

private val localeId = Locale.forLanguageTag("id-ID")

data class CategoryBar(val label: String, val total: Long, val fraction: Float)

/** Satu kolom tren bulanan dengan tinggi relatif (0..1) untuk masuk & keluar. */
data class TrendBar(
    val label: String,
    val masuk: Long,
    val keluar: Long,
    val masukFraction: Float,
    val keluarFraction: Float,
)

data class ReportsUiState(
    val loading: Boolean = true,
    val masuk: Long = 0,
    val keluar: Long = 0,
    val recapTotal: Long = 0,
    val bars: List<CategoryBar> = emptyList(),
    val trend: List<TrendBar> = emptyList(),
) {
    val isEmpty: Boolean get() = masuk == 0L && keluar == 0L && bars.isEmpty()
    val hasTrend: Boolean get() = trend.any { it.masuk > 0 || it.keluar > 0 }
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    observeExpenseByCategory: ObserveExpenseByCategory,
    observeMonthlyFlow: ObserveMonthlyFlow,
    observeMonthlyTrend: ObserveMonthlyTrend,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Jakarta"))

    val state: StateFlow<ReportsUiState> = combine(
        observeExpenseByCategory(today),
        observeMonthlyFlow(today),
        observeMonthlyTrend(today),
    ) { recap, monthly, trend ->
        ReportsUiState(
            loading = false,
            masuk = monthly.masuk,
            keluar = monthly.keluar,
            recapTotal = recap.sumOf { it.total },
            bars = toBars(recap),
            trend = toTrend(trend),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReportsUiState(),
    )

    private fun toTrend(points: List<MonthlyPoint>): List<TrendBar> {
        // Skala bersama (masuk & keluar) agar tinggi antar-bulan sebanding.
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
