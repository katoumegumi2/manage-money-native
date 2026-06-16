package com.taufik.managemoney.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.repo.AccountRepository
import com.taufik.managemoney.domain.model.Account
import com.taufik.managemoney.domain.model.AccountType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

private val localeId = Locale.forLanguageTag("id-ID")

data class AccountRow(val account: Account, val saldo: Long)

data class AccountsUiState(
    val loading: Boolean = true,
    val items: List<AccountRow> = emptyList(),
) {
    val activeCount: Int get() = items.count { it.account.aktif }
}

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val errorFlow = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = errorFlow

    // Layar Kelola akun menampilkan SEMUA akun (aktif + nonaktif) agar bisa di-toggle.
    val state: StateFlow<AccountsUiState> = combine(
        accountRepository.observeAllAccounts(),
        accountRepository.observeAllBalances(),
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

    /** Aktif/nonaktifkan akun. Akun nonaktif hilang dari Beranda & pilihan input. */
    fun setActive(account: Account, aktif: Boolean) {
        if (!aktif && state.value.activeCount <= 1) {
            errorFlow.value = "Minimal 1 akun harus aktif"
            return
        }
        viewModelScope.launch {
            accountRepository.update(account.copy(aktif = aktif))
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
