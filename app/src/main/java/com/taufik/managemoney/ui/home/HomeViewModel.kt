package com.taufik.managemoney.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.domain.model.AccountBalance
import com.taufik.managemoney.domain.usecase.ObserveBalances
import com.taufik.managemoney.domain.usecase.ObserveMonthlyFlow
import com.taufik.managemoney.domain.usecase.ObserveRecentTransactions
import com.taufik.managemoney.domain.usecase.ObserveTotal
import com.taufik.managemoney.ui.model.TxRowUi
import com.taufik.managemoney.ui.model.toRowUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeBalances: ObserveBalances,
    observeTotal: ObserveTotal,
    observeRecent: ObserveRecentTransactions,
    observeMonthlyFlow: ObserveMonthlyFlow,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Jakarta"))

    val state: StateFlow<HomeUiState> = combine(
        observeBalances(),
        observeTotal(),
        observeRecent(),
        observeMonthlyFlow(today),
    ) { balances, total, recent, monthly ->
        val namesById = balances.associate { it.accountId to it.nama }
        HomeUiState(
            loading = false,
            total = total,
            jumlahAkun = balances.size,
            masuk = monthly.masuk,
            keluar = monthly.keluar,
            accounts = balances,
            recent = recent.map { it.toRowUi(namesById, today) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )
}
