package com.taufik.managemoney.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.components.AccountPicker
import com.taufik.managemoney.ui.components.CatatanField
import com.taufik.managemoney.ui.components.KategoriPicker
import com.taufik.managemoney.ui.components.NominalField
import com.taufik.managemoney.ui.components.TanggalField
import com.taufik.managemoney.ui.components.TipeSegmented
import com.taufik.managemoney.ui.components.TransferAccounts
import com.taufik.managemoney.ui.theme.Expense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    onDone: () -> Unit,
    viewModel: EditTransactionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffectDone(state.saved || state.deleted, onDone)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_title)) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (state.found) {
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.action_delete), tint = Expense)
                        }
                    }
                },
            )
        },
    ) { padding ->
        val editable = state.editable
        if (!state.found && !state.loading) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text(stringResource(R.string.edit_not_found), style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }
        if (editable == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TipeSegmented(selected = editable.tipe, onSelect = viewModel::setTipe)
            NominalField(value = editable.nominal, onValue = viewModel::setNominal)

            if (editable.tipe != TxType.TRANSFER) {
                KategoriPicker(tipe = editable.tipe, selected = editable.kategori, onSelect = viewModel::setKategori)
            }

            when (editable.tipe) {
                TxType.PENGELUARAN -> AccountPicker(
                    label = stringResource(R.string.field_account_from),
                    accounts = state.accounts,
                    selectedId = editable.akunDariId,
                    onSelect = viewModel::setAkunDari,
                )
                TxType.PEMASUKAN -> AccountPicker(
                    label = stringResource(R.string.field_account_to),
                    accounts = state.accounts,
                    selectedId = editable.akunKeId,
                    onSelect = viewModel::setAkunKe,
                )
                TxType.TRANSFER -> TransferAccounts(
                    accounts = state.accounts,
                    fromId = editable.akunDariId,
                    toId = editable.akunKeId,
                    onFrom = viewModel::setAkunDari,
                    onTo = viewModel::setAkunKe,
                    fromError = false,
                    toError = false,
                )
            }

            TanggalField(value = editable.tanggal, onValue = viewModel::setTanggal)
            CatatanField(value = editable.catatan, onValue = viewModel::setCatatan)

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = viewModel::save,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text(stringResource(R.string.edit_delete_title)) },
            text = { Text(stringResource(R.string.edit_delete_msg)) },
            confirmButton = {
                TextButton(onClick = { showDelete = false; viewModel.delete() }) {
                    Text(stringResource(R.string.action_delete), color = Expense)
                }
            },
            dismissButton = {
                if (state.batchId != null) {
                    TextButton(onClick = { showDelete = false; viewModel.deleteBatch() }) {
                        Text(stringResource(R.string.edit_delete_batch), color = Expense)
                    }
                } else {
                    TextButton(onClick = { showDelete = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            },
        )
    }
}

@Composable
private fun LaunchedEffectDone(done: Boolean, onDone: () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(done) { if (done) onDone() }
}
