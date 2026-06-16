package com.taufik.managemoney.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.db.DatabaseBackup
import com.taufik.managemoney.data.export.CsvExporter
import com.taufik.managemoney.data.repo.AccountRepository
import com.taufik.managemoney.data.repo.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Hasil aksi data (ekspor/cadangkan/pulihkan) untuk umpan balik UI. */
sealed interface DataResult {
    data class Exported(val ok: Boolean) : DataResult
    data class BackedUp(val ok: Boolean) : DataResult
    data class Restored(val ok: Boolean) : DataResult
}

@HiltViewModel
class DataBackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val databaseBackup: DatabaseBackup,
) : ViewModel() {

    fun exportCsv(uri: Uri, onResult: (DataResult) -> Unit) {
        viewModelScope.launch {
            val ok = runCatching {
                val txs = transactionRepository.observeAll().first()
                val kodeById = accountRepository.observeActiveAccounts().first().associate { it.id to it.kode }
                val csv = CsvExporter.export(txs, kodeById)
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
                        ?: error("stream null")
                }
            }.isSuccess
            onResult(DataResult.Exported(ok))
        }
    }

    fun backup(uri: Uri, onResult: (DataResult) -> Unit) {
        viewModelScope.launch {
            val ok = runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { databaseBackup.backup(it) }
                        ?: error("stream null")
                }
            }.isSuccess
            onResult(DataResult.BackedUp(ok))
        }
    }

    fun restore(uri: Uri, onResult: (DataResult) -> Unit) {
        viewModelScope.launch {
            val ok = runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { databaseBackup.restore(it) }
                        ?: error("stream null")
                }
            }.isSuccess
            onResult(DataResult.Restored(ok))
        }
    }
}
