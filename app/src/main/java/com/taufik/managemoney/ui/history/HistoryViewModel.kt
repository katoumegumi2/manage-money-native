package com.taufik.managemoney.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.domain.model.Account
import com.taufik.managemoney.domain.model.CategorySum
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.domain.usecase.ObserveAccounts
import com.taufik.managemoney.domain.usecase.ObserveAllTransactions
import com.taufik.managemoney.domain.usecase.ObserveExpenseByCategory
import com.taufik.managemoney.ui.model.TxRowUi
import com.taufik.managemoney.ui.model.toRowUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

private val localeId = Locale.forLanguageTag("id-ID")
private val dateHeaderFormat = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", localeId)

data class HistoryFilter(
    val tipe: TxType? = null,
    val accountId: Long? = null,
    val query: String = "",
)

/** Satu kategori pada rekap: label + total + porsi (0..1) untuk bar. */
data class CategoryBar(val label: String, val total: Long, val fraction: Float)

/** Kelompok transaksi per tanggal. */
data class DateGroup(val label: String, val rows: List<TxRowUi>)

data class HistoryUiState(
    val loading: Boolean = true,
    val filter: HistoryFilter = HistoryFilter(),
    val accounts: List<Account> = emptyList(),
    val recap: List<CategoryBar> = emptyList(),
    val recapTotal: Long = 0,
    val groups: List<DateGroup> = emptyList(),
) {
    val isEmpty: Boolean get() = groups.isEmpty()
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observeAll: ObserveAllTransactions,
    observeAccounts: ObserveAccounts,
    observeExpenseByCategory: ObserveExpenseByCategory,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Jakarta"))
    private val filter = MutableStateFlow(HistoryFilter())

    val state: StateFlow<HistoryUiState> = combine(
        observeAll(),
        observeAccounts(),
        observeExpenseByCategory(today),
        filter,
    ) { transactions, accounts, recapRows, f ->
        val namesById = accounts.associate { it.id to it.nama }

        val filtered = transactions.filter { tx ->
            (f.tipe == null || tx.tipe == f.tipe) &&
                (f.accountId == null || tx.akunDariId == f.accountId || tx.akunKeId == f.accountId) &&
                (f.query.isBlank() || matchesQuery(tx.catatan, tx.kategori, f.query))
        }

        val groups = filtered
            .groupBy { it.tanggal }
            .toSortedMap(compareByDescending { it })
            .map { (date, list) ->
                DateGroup(
                    label = date.format(dateHeaderFormat),
                    rows = list.map { it.toRowUi(namesById, today) },
                )
            }

        HistoryUiState(
            loading = false,
            filter = f,
            accounts = accounts,
            recap = toBars(recapRows),
            recapTotal = recapRows.sumOf { it.total },
            groups = groups,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun setTipe(tipe: TxType?) = filter.update { it.copy(tipe = tipe) }
    fun setAccount(accountId: Long?) = filter.update { it.copy(accountId = accountId) }
    fun setQuery(query: String) = filter.update { it.copy(query = query) }

    private fun matchesQuery(catatan: String, kategori: String, q: String): Boolean {
        val needle = q.trim().lowercase(localeId)
        return catatan.lowercase(localeId).contains(needle) ||
            kategori.lowercase(localeId).contains(needle)
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
