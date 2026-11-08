package com.taufik.managemoney.ui.edit

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import com.taufik.managemoney.ui.components.CircleIconButton
import com.taufik.managemoney.ui.components.PrimaryPillButton
import com.taufik.managemoney.ui.components.SubScreenHeader
import com.taufik.managemoney.ui.components.appCard
import com.taufik.managemoney.ui.components.screenBackground

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
import androidx.compose.foundation.layout.WindowInsets
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

    val c = com.taufik.managemoney.ui.theme.AppTheme.colors
    Column(
        Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding().imePadding(),
    ) {
        SubScreenHeader(
            stringResource(R.string.edit_title),
            onBack = onDone,
            trailing = {
                if (state.found) {
                    CircleIconButton(R.drawable.sol_trash, { showDelete = true }, stringResource(R.string.action_delete), tint = c.danger)
                } else {
                    Spacer(Modifier.size(42.dp))
                }
            },
        )
        val editable = state.editable
        if (!state.found && !state.loading) {
            Text(stringResource(R.string.edit_not_found), style = MaterialTheme.typography.bodyLarge, color = c.muted, modifier = Modifier.padding(24.dp))
            return@Column
        }
        if (editable == null) return@Column
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                Modifier.fillMaxWidth().appCard(androidx.compose.foundation.shape.RoundedCornerShape(26.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TipeSegmented(selected = editable.tipe, onSelect = viewModel::setTipe)
                NominalField(value = editable.nominal, onValue = viewModel::setNominal)
                if (editable.tipe != TxType.TRANSFER) {
                    KategoriPicker(tipe = editable.tipe, selected = editable.kategori, onSelect = viewModel::setKategori)
                }
                when (editable.tipe) {
                    TxType.PENGELUARAN -> AccountPicker(stringResource(R.string.field_account_from), state.accounts, editable.akunDariId, viewModel::setAkunDari)
                    TxType.PEMASUKAN -> AccountPicker(stringResource(R.string.field_account_to), state.accounts, editable.akunKeId, viewModel::setAkunKe)
                    TxType.TRANSFER -> TransferAccounts(state.accounts, editable.akunDariId, editable.akunKeId, viewModel::setAkunDari, viewModel::setAkunKe, fromError = false, toError = false)
                }
                TanggalField(value = editable.tanggal, onValue = viewModel::setTanggal)
                CatatanField(value = editable.catatan, onValue = viewModel::setCatatan)
            }
            state.error?.let { Text(it, color = c.danger, style = MaterialTheme.typography.bodySmall) }
        }
        PrimaryPillButton(
            stringResource(R.string.action_save),
            onClick = viewModel::save,
            enabled = state.canSave,
            icon = R.drawable.sol_check,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text(stringResource(R.string.edit_delete_title)) },
            text = { Text(stringResource(R.string.edit_delete_msg)) },
            confirmButton = {
                TextButton(onClick = { showDelete = false; viewModel.delete() }) {
                    Text(stringResource(R.string.action_delete), color = com.taufik.managemoney.ui.theme.AppTheme.colors.danger)
                }
            },
            dismissButton = {
                if (state.batchId != null) {
                    TextButton(onClick = { showDelete = false; viewModel.deleteBatch() }) {
                        Text(stringResource(R.string.edit_delete_batch), color = com.taufik.managemoney.ui.theme.AppTheme.colors.danger)
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
