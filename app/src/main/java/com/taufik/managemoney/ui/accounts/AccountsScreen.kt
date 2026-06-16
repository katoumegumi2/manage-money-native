package com.taufik.managemoney.ui.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.core.formatRupiah
import com.taufik.managemoney.domain.model.Account
import com.taufik.managemoney.domain.model.AccountType
import com.taufik.managemoney.ui.components.AccountBadge
import com.taufik.managemoney.ui.components.NominalField
import com.taufik.managemoney.ui.theme.MonoFamily
import com.taufik.managemoney.ui.util.accountVisual
import com.taufik.managemoney.ui.util.jenisLabel

@Composable
fun AccountsScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Account?>(null) }
    var adding by remember { mutableStateOf(false) }

    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        item {
            Text(
                text = stringResource(R.string.nav_accounts),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 14.dp, bottom = 8.dp),
            )
        }
        items(state.items, key = { it.account.id }) { row ->
            AccountListItem(
                account = row.account,
                saldo = row.saldo,
                onClick = { editing = row.account },
            )
        }
        item {
            OutlinedButton(
                onClick = { adding = true },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            ) {
                Text(stringResource(R.string.accounts_add))
            }
        }
        item { Spacer(Modifier.height(96.dp)) }
    }

    editing?.let { acc ->
        EditAccountDialog(
            account = acc,
            onDismiss = { editing = null },
            onSave = { nama, saldoAwal ->
                viewModel.updateAccount(acc, nama, saldoAwal)
                editing = null
            },
        )
    }

    if (adding) {
        AddAccountDialog(
            onDismiss = { adding = false },
            onAdd = { kode, nama, jenis, saldoAwal ->
                viewModel.addAccount(kode, nama, jenis, saldoAwal)
                adding = false
            },
        )
    }
}

@Composable
private fun AccountListItem(account: Account, saldo: Long, onClick: () -> Unit) {
    val v = accountVisual(account.kode, account.jenis)
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AccountBadge(abbr = v.abbr, color = v.color, size = 38)
        Column(modifier = Modifier.weight(1f).padding(horizontal = 13.dp)) {
            Text(account.nama, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                text = stringResource(R.string.account_initial_balance, formatRupiah(account.saldoAwal)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = formatRupiah(saldo),
            fontFamily = MonoFamily,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun EditAccountDialog(account: Account, onDismiss: () -> Unit, onSave: (String, Long) -> Unit) {
    var nama by remember { mutableStateOf(account.nama) }
    var saldoAwal by remember { mutableStateOf(account.saldoAwal) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.account_edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text(stringResource(R.string.account_name)) },
                    singleLine = true,
                )
                NominalField(value = saldoAwal, onValue = { saldoAwal = it })
            }
        },
        confirmButton = { TextButton(onClick = { onSave(nama, saldoAwal) }, enabled = nama.isNotBlank()) { Text(stringResource(R.string.action_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAccountDialog(onDismiss: () -> Unit, onAdd: (String, String, AccountType, Long) -> Unit) {
    var kode by remember { mutableStateOf("") }
    var nama by remember { mutableStateOf("") }
    var jenis by remember { mutableStateOf(AccountType.BANK) }
    var saldoAwal by remember { mutableStateOf(0L) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.accounts_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = kode, onValueChange = { kode = it }, label = { Text(stringResource(R.string.account_code)) }, singleLine = true)
                OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text(stringResource(R.string.account_name)) }, singleLine = true)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AccountType.entries.forEachIndexed { i, t ->
                        SegmentedButton(
                            selected = t == jenis,
                            onClick = { jenis = t },
                            shape = SegmentedButtonDefaults.itemShape(i, AccountType.entries.size),
                        ) { Text(jenisLabel(t)) }
                    }
                }
                NominalField(value = saldoAwal, onValue = { saldoAwal = it })
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(kode, nama, jenis, saldoAwal) },
                enabled = kode.isNotBlank() && nama.isNotBlank(),
            ) { Text(stringResource(R.string.action_add)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
