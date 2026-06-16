package com.taufik.managemoney.ui.manualentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.repo.AccountRepository
import com.taufik.managemoney.domain.model.Account
import com.taufik.managemoney.domain.model.Categories
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.model.EditableTx
import com.taufik.managemoney.ui.model.toParsed
import com.taufik.managemoney.ui.navigation.ConfirmPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class ManualEntryUiState(
    val accounts: List<Account> = emptyList(),
    val draft: EditableTx,
) {
    /** Boleh lanjut ke konfirmasi bila nominal sudah diisi (akun divalidasi di Konfirmasi). */
    val canContinue: Boolean get() = draft.nominal > 0
}

@HiltViewModel
class ManualEntryViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Jakarta"))

    private val _state = MutableStateFlow(
        ManualEntryUiState(
            draft = EditableTx(
                tipe = TxType.PENGELUARAN,
                nominal = 0,
                kategori = Categories.defaultFor(TxType.PENGELUARAN),
                akunDariId = null,
                akunKeId = null,
                tanggal = today,
                catatan = "",
            ),
        ),
    )
    val state: StateFlow<ManualEntryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val accounts = accountRepository.observeActiveAccounts().first()
            _state.update { it.copy(accounts = accounts) }
        }
    }

    private fun edit(transform: (EditableTx) -> EditableTx) =
        _state.update { it.copy(draft = transform(it.draft)) }

    fun setTipe(tipe: TxType) = edit { it.copy(tipe = tipe, kategori = Categories.defaultFor(tipe)) }
    fun setNominal(value: Long) = edit { it.copy(nominal = value) }
    fun setKategori(value: String) = edit { it.copy(kategori = value) }
    fun setAkunDari(id: Long) = edit { it.copy(akunDariId = id) }
    fun setAkunKe(id: Long) = edit { it.copy(akunKeId = id) }
    fun setTanggal(value: LocalDate) = edit { it.copy(tanggal = value) }
    fun setCatatan(value: String) = edit { it.copy(catatan = value) }

    /** Encode draft jadi payload `List<ParsedTransaction>` untuk dioper ke layar Konfirmasi. */
    fun buildPayload(): String {
        val s = _state.value
        val kodeById = s.accounts.associate { it.id to it.kode }
        return ConfirmPayload.encode(listOf(s.draft.toParsed(kodeById)))
    }
}
