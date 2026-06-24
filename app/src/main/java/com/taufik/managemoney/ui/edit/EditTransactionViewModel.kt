package com.taufik.managemoney.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.repo.TransactionRepository
import com.taufik.managemoney.domain.model.Account
import com.taufik.managemoney.domain.model.Categories
import com.taufik.managemoney.domain.model.Transaction
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.domain.usecase.DeleteTransaction
import com.taufik.managemoney.domain.usecase.DeleteTransactionBatch
import com.taufik.managemoney.domain.usecase.ObserveAccounts
import com.taufik.managemoney.domain.usecase.UpdateTransaction
import com.taufik.managemoney.ui.model.EditableTx
import com.taufik.managemoney.ui.navigation.ARG_TX_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class EditTxUiState(
    val loading: Boolean = true,
    val found: Boolean = true,
    val editable: EditableTx? = null,
    val accounts: List<Account> = emptyList(),
    val batchId: String? = null,
    val saved: Boolean = false,
    val deleted: Boolean = false,
    val error: String? = null,
) {
    val canSave: Boolean get() = editable?.valid == true
}

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    observeAccounts: ObserveAccounts,
    private val updateTransaction: UpdateTransaction,
    private val deleteTransaction: DeleteTransaction,
    private val deleteTransactionBatch: DeleteTransactionBatch,
) : ViewModel() {

    private val txId: String = savedStateHandle.get<String>(ARG_TX_ID) ?: ""
    private var original: Transaction? = null

    private val _state = MutableStateFlow(EditTxUiState())
    val state: StateFlow<EditTxUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val accounts = observeAccounts().first()
            val tx = transactionRepository.getById(txId)
            if (tx == null) {
                _state.update { it.copy(loading = false, found = false) }
            } else {
                original = tx
                _state.update {
                    it.copy(
                        loading = false,
                        found = true,
                        accounts = accounts,
                        batchId = tx.batchId,
                        editable = EditableTx(
                            tipe = tx.tipe,
                            nominal = tx.nominal,
                            kategori = tx.kategori,
                            akunDariId = tx.akunDariId,
                            akunKeId = tx.akunKeId,
                            tanggal = tx.tanggal,
                            catatan = tx.catatan,
                        ),
                    )
                }
            }
        }
    }

    private fun edit(transform: (EditableTx) -> EditableTx) =
        _state.update { s -> s.copy(editable = s.editable?.let(transform), error = null) }

    fun setTipe(tipe: TxType) = edit {
        if (it.tipe == tipe) return@edit it
        // ganti tipe → pertahankan kategori bila masih valid, lalu bersihkan akun yang tak relevan
        // (pemasukan tak pakai "dari", pengeluaran tak pakai "ke") agar tak ada FK nyasar / akun dari==ke.
        val kategori = if (it.kategori in Categories.forType(tipe)) it.kategori else Categories.defaultFor(tipe)
        it.copy(
            tipe = tipe,
            kategori = kategori,
            akunDariId = if (tipe == TxType.PEMASUKAN) null else it.akunDariId,
            akunKeId = if (tipe == TxType.PENGELUARAN) null else it.akunKeId,
        )
    }

    fun setNominal(value: Long) = edit { it.copy(nominal = value) }
    fun setKategori(value: String) = edit { it.copy(kategori = value) }
    fun setAkunDari(id: String) = edit { it.copy(akunDariId = id) }
    fun setAkunKe(id: String) = edit { it.copy(akunKeId = id) }
    fun setTanggal(date: LocalDate) = edit { it.copy(tanggal = date) }
    fun setCatatan(value: String) = edit { it.copy(catatan = value) }

    fun save() {
        val editable = _state.value.editable ?: return
        val base = original ?: return
        viewModelScope.launch {
            val edited = base.copy(
                tipe = editable.tipe,
                nominal = editable.nominal,
                kategori = editable.kategori,
                akunDariId = editable.akunDariId,
                akunKeId = editable.akunKeId,
                tanggal = editable.tanggal,
                catatan = editable.catatan,
            )
            updateTransaction(edited)
                .onSuccess { _state.update { it.copy(saved = true) } }
                .onFailure { e -> _state.update { it.copy(error = e.message ?: "Gagal menyimpan") } }
        }
    }

    fun delete() {
        viewModelScope.launch {
            deleteTransaction(txId)
            _state.update { it.copy(deleted = true) }
        }
    }

    fun deleteBatch() {
        val batchId = _state.value.batchId ?: return delete()
        viewModelScope.launch {
            deleteTransactionBatch(batchId)
            _state.update { it.copy(deleted = true) }
        }
    }
}
