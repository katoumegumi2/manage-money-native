package com.taufik.managemoney.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.settings.HomeAccountOrderStore
import com.taufik.managemoney.domain.model.AccountBalance
import com.taufik.managemoney.domain.usecase.ObserveBalances
import com.taufik.managemoney.domain.usecase.MonthlyPoint
import com.taufik.managemoney.domain.usecase.ObserveMonthlyFlow
import com.taufik.managemoney.domain.usecase.ObserveMonthlyTrend
import com.taufik.managemoney.domain.usecase.ObserveRecentTransactions
import com.taufik.managemoney.domain.usecase.ObserveTotal
import com.taufik.managemoney.ui.model.TxRowUi
import com.taufik.managemoney.ui.model.toRowUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val total: Long = 0,
    val jumlahAkun: Int = 0,
    val masuk: Long = 0,
    val keluar: Long = 0,
    val accounts: List<AccountBalance> = emptyList(),
    val recent: List<TxRowUi> = emptyList(),
    /** Tren 6 bulan (bulan terakhir = berjalan) untuk grafik Beranda. */
    val trend: List<MonthlyPoint> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeBalances: ObserveBalances,
    observeTotal: ObserveTotal,
    observeRecent: ObserveRecentTransactions,
    observeMonthlyFlow: ObserveMonthlyFlow,
    observeMonthlyTrend: ObserveMonthlyTrend,
    private val orderStore: HomeAccountOrderStore,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Jakarta"))

    private val base = combine(
        observeBalances(),
        observeTotal(),
        observeRecent(),
        observeMonthlyFlow(today),
        orderStore.order,
    ) { balances, total, recent, monthly, order ->
        val ordered = applyOrder(balances, order)
        val namesById = balances.associate { it.accountId to it.nama }
        HomeUiState(
            loading = false,
            total = total,
            jumlahAkun = balances.size,
            masuk = monthly.masuk,
            keluar = monthly.keluar,
            accounts = ordered,
            recent = recent.map { it.toRowUi(namesById, today) },
        )
    }

    val state: StateFlow<HomeUiState> = combine(base, observeMonthlyTrend(today)) { s, trend ->
        s.copy(trend = trend)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    /** Simpan urutan baru hasil drag-reorder di Beranda. */
    fun reorder(ids: List<String>) {
        viewModelScope.launch { orderStore.save(ids) }
    }

    /** Susun ulang akun mengikuti [order] tersimpan; akun yang belum ada di order ikut di belakang. */
    private fun applyOrder(balances: List<AccountBalance>, order: List<String>): List<AccountBalance> {
        if (order.isEmpty()) return balances
        val pos = order.withIndex().associate { (i, id) -> id to i }
        return balances.sortedBy { pos[it.accountId] ?: Int.MAX_VALUE }
    }
}
