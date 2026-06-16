package com.taufik.managemoney.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.repo.AccountRepository
import com.taufik.managemoney.domain.model.Account
import com.taufik.managemoney.domain.model.AccountType
import com.taufik.managemoney.domain.usecase.ObserveAccounts
import com.taufik.managemoney.domain.usecase.ObserveBalances
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

private val localeId = Locale.forLanguageTag("id-ID")

data class AccountRow(val account: Account, val saldo: Long)

data class AccountsUiState(
    val loading: Boolean = true,
    val items: List<AccountRow> = emptyList(),
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    observeAccounts: ObserveAccounts,
    observeBalances: ObserveBalances,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val errorFlow = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = errorFlow

    val state: StateFlow<AccountsUiState> = combine(
        observeAccounts(),
        observeBalances(),
    ) { accounts, balances ->
        val saldoById = balances.associate { it.accountId to it.saldo }
        AccountsUiState(
            loading = false,
            items = accounts.map { AccountRow(it, saldoById[it.id] ?: it.saldoAwal) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountsUiState(),
    )

    /** Edit nama & saldo awal akun. Saldo berjalan ikut menyesuaikan (derived). */
    fun updateAccount(account: Account, nama: String, saldoAwal: Long) {
        viewModelScope.launch {
            accountRepository.update(account.copy(nama = nama.trim(), saldoAwal = saldoAwal))
        }
    }

    fun addAccount(kode: String, nama: String, jenis: AccountType, saldoAwal: Long) {
        viewModelScope.launch {
            val cleanKode = kode.trim().lowercase(localeId)
            if (cleanKode.isBlank() || nama.isBlank()) {
                errorFlow.value = "Kode dan nama wajib diisi"
                return@launch
            }
            val urutan = (state.value.items.maxOfOrNull { it.account.urutan } ?: 0) + 1
            runCatching {
                accountRepository.add(
                    Account(
                        kode = cleanKode,
                        nama = nama.trim(),
                        jenis = jenis,
                        saldoAwal = saldoAwal,
                        urutan = urutan,
                        aktif = true,
                    ),
                )
            }.onFailure { errorFlow.value = "Kode '$cleanKode' sudah dipakai" }
        }
    }

    fun clearError() { errorFlow.value = null }
}
