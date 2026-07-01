package com.taufik.managemoney.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.components.FilterPill
import com.taufik.managemoney.ui.components.PillStyle
import com.taufik.managemoney.ui.components.SearchField
import com.taufik.managemoney.ui.components.TxRow
import com.taufik.managemoney.ui.util.txTypeLabel

@Composable
fun HistoryScreen(
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        item {
            Text(
                text = stringResource(R.string.nav_history),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 14.dp),
            )
        }

        item {
            SearchField(
                value = state.filter.query,
                onValueChange = viewModel::setQuery,
                placeholder = stringResource(R.string.history_search),
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
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 18.dp, bottom = 2.dp),
                    )
                }
                items(group.rows, key = { it.id }) { row ->
                    TxRow(row, modifier = Modifier.clickable { onEdit(row.id) }, trailingChevron = true)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun TipeFilterRow(selected: TxType?, onSelect: (TxType?) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterPill(
            text = stringResource(R.string.history_all),
            selected = selected == null,
            onClick = { onSelect(null) },
        )
        TxType.entries.forEach { tipe ->
            FilterPill(
                text = txTypeLabel(tipe),
                selected = selected == tipe,
                onClick = { onSelect(if (selected == tipe) null else tipe) },
            )
        }
    }
}

@Composable
private fun AccountFilterRow(state: HistoryUiState, onSelect: (String?) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterPill(
            text = stringResource(R.string.history_account_all),
            selected = state.filter.accountId == null,
            onClick = { onSelect(null) },
            style = PillStyle.ACCENT,
        )
        state.accounts.forEach { acc ->
            FilterPill(
                text = acc.nama,
                selected = state.filter.accountId == acc.id,
                onClick = { onSelect(if (state.filter.accountId == acc.id) null else acc.id) },
                style = PillStyle.ACCENT,
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
