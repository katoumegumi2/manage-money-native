package com.taufik.managemoney.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.core.formatGrouped
import com.taufik.managemoney.core.formatRupiah
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.components.TxRow
import com.taufik.managemoney.ui.theme.Expense
import com.taufik.managemoney.ui.theme.MonoFamily
import com.taufik.managemoney.ui.util.txTypeLabel

@Composable
fun HistoryScreen(
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        item {
            Text(
                text = stringResource(R.string.nav_history),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 14.dp, bottom = 10.dp),
            )
        }

        item {
            OutlinedTextField(
                value = state.filter.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text(stringResource(R.string.history_search)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item { TipeFilterRow(state.filter.tipe, viewModel::setTipe) }
        item { AccountFilterRow(state, viewModel::setAccount) }
        item { Spacer(Modifier.height(8.dp)) }

        if (state.isEmpty && !state.loading) {
            item { EmptyHistory() }
        } else {
            state.groups.forEach { group ->
                item(key = "h-${group.label}") {
                    Text(
                        text = group.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 18.dp, bottom = 2.dp),
                    )
                }
                items(group.rows, key = { it.id }) { row ->
                    TxRow(row, modifier = Modifier.clickable { onEdit(row.id) }, trailingChevron = true)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun TipeFilterRow(selected: TxType?, onSelect: (TxType?) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text(stringResource(R.string.history_all)) })
        TxType.entries.forEach { tipe ->
            FilterChip(
                selected = selected == tipe,
                onClick = { onSelect(if (selected == tipe) null else tipe) },
                label = { Text(txTypeLabel(tipe)) },
            )
        }
    }
}

@Composable
private fun AccountFilterRow(state: HistoryUiState, onSelect: (String?) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = state.filter.accountId == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(R.string.history_account_all)) },
        )
        state.accounts.forEach { acc ->
            FilterChip(
                selected = state.filter.accountId == acc.id,
                onClick = { onSelect(if (state.filter.accountId == acc.id) null else acc.id) },
                label = { Text(acc.nama) },
            )
        }
    }
}

@Composable
private fun EmptyHistory() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.history_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
