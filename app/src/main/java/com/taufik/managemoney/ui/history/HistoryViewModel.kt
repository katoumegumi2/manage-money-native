package com.taufik.managemoney.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.domain.model.Account
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.domain.usecase.ObserveAccounts
import com.taufik.managemoney.domain.usecase.ObserveAllTransactions
import com.taufik.managemoney.ui.model.TxRowUi
import com.taufik.managemoney.ui.model.toRowUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flatMapLatest
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
    val period: com.taufik.managemoney.domain.usecase.HistoryPeriod = com.taufik.managemoney.domain.usecase.HistoryPeriod.MONTH,
    val tipe: TxType? = null,
    val accountId: String? = null,
    val query: String = "",
)

/** Kelompok transaksi per tanggal + total masuk/keluar hari itu (transfer tak dihitung). */
data class DateGroup(val label: String, val rows: List<TxRowUi>, val masuk: Long = 0, val keluar: Long = 0)

data class HistoryUiState(
    val loading: Boolean = true,
    val filter: HistoryFilter = HistoryFilter(),
    val accounts: List<Account> = emptyList(),
    val groups: List<DateGroup> = emptyList(),
    val totalMasuk: Long = 0,
    val totalKeluar: Long = 0,
    val count: Int = 0,
) {
    val isEmpty: Boolean get() = groups.isEmpty()
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observeAll: ObserveAllTransactions,
    observeAccounts: ObserveAccounts,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Jakarta"))
    private val filter = MutableStateFlow(HistoryFilter())

    /** Hanya ambil transaksi periode terpilih dari DB (bukan seluruh riwayat); batas atas terbuka. */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val periodTx = filter
        .map { it.period }
        .distinctUntilChanged()
        .flatMapLatest { p ->
            val (from, _) = com.taufik.managemoney.domain.usecase.historyRange(p, today)
            observeAll(from, java.time.LocalDate.of(9999, 12, 31))
        }

    val state: StateFlow<HistoryUiState> = combine(
        periodTx,
        observeAccounts(),
        filter,
    ) { transactions, accounts, f ->
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
                    label = when (date) {
                        today -> "Hari ini"
                        today.minusDays(1) -> "Kemarin"
                        else -> date.format(dateHeaderFormat).replaceFirstChar { it.titlecase(localeId) }
                    },
                    rows = list.map { it.toRowUi(namesById, today) },
                    masuk = list.filter { it.tipe == TxType.PEMASUKAN }.sumOf { it.nominal },
                    keluar = list.filter { it.tipe == TxType.PENGELUARAN }.sumOf { it.nominal },
                )
            }

        HistoryUiState(
            loading = false,
            filter = f,
            accounts = accounts,
            groups = groups,
            totalMasuk = filtered.filter { it.tipe == TxType.PEMASUKAN }.sumOf { it.nominal },
            totalKeluar = filtered.filter { it.tipe == TxType.PENGELUARAN }.sumOf { it.nominal },
            count = filtered.size,
        )
    }.flowOn(kotlinx.coroutines.Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun setPeriod(p: com.taufik.managemoney.domain.usecase.HistoryPeriod) = filter.update { it.copy(period = p) }

    fun setTipe(tipe: TxType?) = filter.update { it.copy(tipe = tipe) }
    fun setAccount(accountId: String?) = filter.update { it.copy(accountId = accountId) }
    fun setQuery(query: String) = filter.update { it.copy(query = query) }

    private fun matchesQuery(catatan: String, kategori: String, q: String): Boolean {
        val needle = q.trim().lowercase(localeId)
        return catatan.lowercase(localeId).contains(needle) ||
            kategori.lowercase(localeId).contains(needle)
    }
}
