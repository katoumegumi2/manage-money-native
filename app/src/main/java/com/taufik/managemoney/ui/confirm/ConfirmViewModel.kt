package com.taufik.managemoney.ui.confirm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.repo.AccountRepository
import com.taufik.managemoney.domain.model.Account
import com.taufik.managemoney.domain.model.SourceInput
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.domain.usecase.SaveTransactionBatch
import com.taufik.managemoney.ui.model.EditableTx
import com.taufik.managemoney.ui.model.toEditable
import com.taufik.managemoney.ui.navigation.ARG_CONFIRM_PAYLOAD
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

data class ConfirmUiState(
    val loading: Boolean = true,
    val accounts: List<Account> = emptyList(),
    val items: List<EditableTx> = emptyList(),
    val saving: Boolean = false,
    val showErrors: Boolean = false,
    val errorMessage: String? = null,
    val savedCount: Int? = null,
) {
    val canSave: Boolean get() = items.isNotEmpty() && items.all { it.valid }
}

@HiltViewModel
class ConfirmViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val saveTransactionBatch: SaveTransactionBatch,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Jakarta"))
    private val payloadArg: String = savedStateHandle[ARG_CONFIRM_PAYLOAD] ?: ""

    private val _state = MutableStateFlow(ConfirmUiState())
    val state: StateFlow<ConfirmUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val accounts = accountRepository.observeActiveAccounts().first()
            val idByKode = accounts.associate { it.kode to it.id }
            val parsed = runCatching { ConfirmPayload.decode(payloadArg) }.getOrDefault(emptyList())
            _state.update {
                it.copy(
                    loading = false,
                    accounts = accounts,
                    items = parsed.map { p -> p.toEditable(idByKode, today) },
                )
            }
        }
    }

    private fun updateItem(index: Int, transform: (EditableTx) -> EditableTx) {
        _state.update { s ->
            s.copy(items = s.items.mapIndexed { i, item -> if (i == index) transform(item) else item })
        }
    }

    fun setNominal(index: Int, value: Long) = updateItem(index) { it.copy(nominal = value, perluDicek = false) }
    fun setKategori(index: Int, value: String) = updateItem(index) { it.copy(kategori = value) }
    fun setAkunDari(index: Int, id: Long) = updateItem(index) { it.copy(akunDariId = id, perluDicek = false) }
    fun setAkunKe(index: Int, id: Long) = updateItem(index) { it.copy(akunKeId = id, perluDicek = false) }
    fun setTanggal(index: Int, value: LocalDate) = updateItem(index) { it.copy(tanggal = value) }
    fun setCatatan(index: Int, value: String) = updateItem(index) { it.copy(catatan = value) }

    fun removeItem(index: Int) {
        _state.update { s -> s.copy(items = s.items.filterIndexed { i, _ -> i != index }) }
    }

    fun dismissError() = _state.update { it.copy(errorMessage = null) }

    fun save() {
        val current = _state.value
        if (!current.canSave) {
            _state.update { it.copy(showErrors = true) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(saving = true, errorMessage = null) }
            val result = saveTransactionBatch(current.items.map { it.toDraft() }, SourceInput.MANUAL)
            result.onSuccess { ids ->
                _state.update { it.copy(saving = false, savedCount = ids.size) }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        saving = false,
                        showErrors = true,
                        errorMessage = e.message ?: "Gagal menyimpan",
                    )
                }
            }
        }
    }

    /** Kategori valid untuk tipe item (dipakai saat tipe transfer hanya boleh "lainnya"). */
    fun typeOf(index: Int): TxType? = _state.value.items.getOrNull(index)?.tipe
}
