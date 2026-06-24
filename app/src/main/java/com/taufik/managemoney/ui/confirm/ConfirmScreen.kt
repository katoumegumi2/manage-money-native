package com.taufik.managemoney.ui.confirm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.taufik.managemoney.R
import com.taufik.managemoney.domain.model.DraftIssue
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.components.AccountPicker
import com.taufik.managemoney.ui.components.CatatanField
import com.taufik.managemoney.ui.components.KategoriPicker
import com.taufik.managemoney.ui.components.NominalField
import com.taufik.managemoney.ui.components.TanggalField
import com.taufik.managemoney.ui.components.TransferAccounts
import com.taufik.managemoney.ui.model.EditableTx
import com.taufik.managemoney.ui.theme.Gold
import com.taufik.managemoney.ui.util.txTypeColor
import com.taufik.managemoney.ui.util.txTypeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmScreen(
    onBack: () -> Unit,
    onSaved: (Int) -> Unit,
    viewModel: ConfirmViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedCount) {
        state.savedCount?.let { onSaved(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.confirm_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
        bottomBar = {
            if (!state.loading) {
                SaveBar(
                    count = state.items.size,
                    enabled = state.canSave && !state.saving,
                    saving = state.saving,
                    onSave = viewModel::save,
                )
            }
        },
    ) { padding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            state.items.isEmpty() -> EmptyConfirm(Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                state.errorMessage?.let { msg ->
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                msg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }
                itemsIndexed(state.items) { index, item ->
                    TxCard(
                        item = item,
                        accounts = state.accounts,
                        showErrors = state.showErrors,
                        canRemove = state.items.size > 1,
                        onTipe = { viewModel.setTipe(index, it) },
                        onNominal = { viewModel.setNominal(index, it) },
                        onKategori = { viewModel.setKategori(index, it) },
                        onAkunDari = { viewModel.setAkunDari(index, it) },
                        onAkunKe = { viewModel.setAkunKe(index, it) },
                        onTanggal = { viewModel.setTanggal(index, it) },
                        onCatatan = { viewModel.setCatatan(index, it) },
                        onRemove = { viewModel.removeItem(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TxCard(
    item: EditableTx,
    accounts: List<com.taufik.managemoney.domain.model.Account>,
    showErrors: Boolean,
    canRemove: Boolean,
    onTipe: (TxType) -> Unit,
    onNominal: (Long) -> Unit,
    onKategori: (String) -> Unit,
    onAkunDari: (String) -> Unit,
    onAkunKe: (String) -> Unit,
    onTanggal: (java.time.LocalDate) -> Unit,
    onCatatan: (String) -> Unit,
    onRemove: () -> Unit,
) {
    val issues = item.issues
    val borderColor = if (item.perluDicek) Gold else Color.Transparent
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (item.perluDicek) {
                    Modifier.border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                } else {
                    Modifier
                },
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TypeSelector(selected = item.tipe, onSelect = onTipe, modifier = Modifier.weight(1f))
                if (canRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (item.perluDicek) {
                Text(
                    stringResource(R.string.confirm_need_check),
                    color = Gold,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            NominalField(
                value = item.nominal,
                onValue = onNominal,
                isError = showErrors && DraftIssue.NOMINAL_TIDAK_VALID in issues,
            )

            if (item.tipe != TxType.TRANSFER) {
                KategoriPicker(tipe = item.tipe, selected = item.kategori, onSelect = onKategori)
            }

            when (item.tipe) {
                TxType.PENGELUARAN -> AccountPicker(
                    label = stringResource(R.string.field_from_short),
                    accounts = accounts,
                    selectedId = item.akunDariId,
                    onSelect = onAkunDari,
                    isError = showErrors && DraftIssue.AKUN_DARI_WAJIB in issues,
                )
                TxType.PEMASUKAN -> AccountPicker(
                    label = stringResource(R.string.field_to_short),
                    accounts = accounts,
                    selectedId = item.akunKeId,
                    onSelect = onAkunKe,
                    isError = showErrors && DraftIssue.AKUN_KE_WAJIB in issues,
                )
                TxType.TRANSFER -> TransferAccounts(
                    accounts = accounts,
                    fromId = item.akunDariId,
                    toId = item.akunKeId,
                    onFrom = onAkunDari,
                    onTo = onAkunKe,
                    fromError = showErrors && DraftIssue.AKUN_DARI_WAJIB in issues,
                    toError = showErrors &&
                        (DraftIssue.AKUN_KE_WAJIB in issues || DraftIssue.TRANSFER_AKUN_SAMA in issues),
                )
            }

            TanggalField(value = item.tanggal, onValue = onTanggal)
            CatatanField(value = item.catatan, onValue = onCatatan)

            if (showErrors && issues.isNotEmpty()) {
                Text(
                    text = issueHint(issues),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

/** Pemilih tipe (pengeluaran/pemasukan/transfer) — user bisa koreksi klasifikasi AI. */
@Composable
private fun TypeSelector(selected: TxType, onSelect: (TxType) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
    ) {
        TxType.entries.forEach { tipe ->
            val isSelected = tipe == selected
            val color = txTypeColor(tipe)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (isSelected) color.copy(alpha = 0.16f) else Color.Transparent)
                    .clickable { onSelect(tipe) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = txTypeLabel(tipe),
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun SaveBar(count: Int, enabled: Boolean, saving: Boolean, onSave: () -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Button(
            onClick = onSave,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            if (saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.size(8.dp))
            }
            Text(if (count > 1) stringResource(R.string.confirm_save_all, count) else stringResource(R.string.action_save))
        }
    }
}

@Composable
private fun EmptyConfirm(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), Alignment.Center) {
        Text(
            stringResource(R.string.confirm_empty),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun issueHint(issues: Set<DraftIssue>): String = when {
    DraftIssue.NOMINAL_TIDAK_VALID in issues -> stringResource(R.string.issue_nominal)
    DraftIssue.TRANSFER_AKUN_SAMA in issues -> stringResource(R.string.issue_transfer_same)
    DraftIssue.AKUN_DARI_WAJIB in issues || DraftIssue.AKUN_KE_WAJIB in issues -> stringResource(R.string.issue_account_required)
    else -> ""
}
