package com.taufik.managemoney.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.domain.model.CategorySum
import com.taufik.managemoney.domain.usecase.ObserveExpenseByCategory
import com.taufik.managemoney.domain.usecase.ObserveMonthlyFlow
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

data class ReportsUiState(
    val loading: Boolean = true,
    val masuk: Long = 0,
    val keluar: Long = 0,
    val recapTotal: Long = 0,
    val bars: List<CategoryBar> = emptyList(),
) {
    val isEmpty: Boolean get() = masuk == 0L && keluar == 0L && bars.isEmpty()
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    observeExpenseByCategory: ObserveExpenseByCategory,
    observeMonthlyFlow: ObserveMonthlyFlow,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Jakarta"))

    val state: StateFlow<ReportsUiState> = combine(
        observeExpenseByCategory(today),
        observeMonthlyFlow(today),
    ) { recap, monthly ->
        ReportsUiState(
            loading = false,
            masuk = monthly.masuk,
            keluar = monthly.keluar,
            recapTotal = recap.sumOf { it.total },
            bars = toBars(recap),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReportsUiState(),
    )

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
