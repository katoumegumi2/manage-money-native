package com.taufik.managemoney.ui.account

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.domain.model.AccountType
import com.taufik.managemoney.domain.usecase.ObserveAccountTransactions
import com.taufik.managemoney.domain.usecase.ObserveAccounts
import com.taufik.managemoney.domain.usecase.ObserveBalances
import com.taufik.managemoney.ui.model.TxRowUi
import com.taufik.managemoney.ui.model.toRowUi
import com.taufik.managemoney.ui.navigation.ARG_ACCOUNT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class AccountDetailUiState(
    val loading: Boolean = true,
    val found: Boolean = true,
    val kode: String = "",
    val nama: String = "",
    val jenis: AccountType? = null,
    val saldo: Long = 0,
    val saldoAwal: Long = 0,
    val mutasi: List<TxRowUi> = emptyList(),
)

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeAccounts: ObserveAccounts,
    observeBalances: ObserveBalances,
    observeAccountTransactions: ObserveAccountTransactions,
) : ViewModel() {

    private val accountId: String = savedStateHandle.get<String>(ARG_ACCOUNT_ID) ?: ""
    private val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Jakarta"))

    val state: StateFlow<AccountDetailUiState> = combine(
        observeAccounts(),
        observeBalances(),
        observeAccountTransactions(accountId),
    ) { accounts, balances, mutasi ->
        val account = accounts.firstOrNull { it.id == accountId }
        val balance = balances.firstOrNull { it.accountId == accountId }
        if (account == null) {
            AccountDetailUiState(loading = false, found = false)
        } else {
            val namesById = balances.associate { it.accountId to it.nama }
            AccountDetailUiState(
                loading = false,
                found = true,
                kode = account.kode,
                nama = account.nama,
                jenis = account.jenis,
                saldo = balance?.saldo ?: account.saldoAwal,
                saldoAwal = account.saldoAwal,
                mutasi = mutasi.map { it.toRowUi(namesById, today, perspectiveId = accountId) },
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountDetailUiState(),
    )
}
