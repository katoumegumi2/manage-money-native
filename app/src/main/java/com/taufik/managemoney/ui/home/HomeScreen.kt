package com.taufik.managemoney.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.taufik.managemoney.domain.model.AccountBalance
import com.taufik.managemoney.ui.components.AccountTile
import com.taufik.managemoney.ui.components.TxRow
import com.taufik.managemoney.ui.theme.Expense
import com.taufik.managemoney.ui.theme.Income
import com.taufik.managemoney.ui.theme.MonoFamily

@Composable
fun HomeScreen(
    onAccountClick: (String) -> Unit,
    onSettings: () -> Unit,
    onAddManual: () -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
    ) {
        item { HeaderSection(onSettings, onAddManual) }
        item { HeroSection(state) }
        item { Spacer(Modifier.height(8.dp)) }

        items(state.accounts.chunked(2)) { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                if (pair.size == 1) {
                    // Tile ganjil terakhir → selebar penuh agar tidak ada kotak kosong.
                    AccountTile(
                        account = pair[0],
                        onClick = { onAccountClick(pair[0].accountId) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    pair.forEach { acc ->
                        AccountTile(
                            account = acc,
                            onClick = { onAccountClick(acc.accountId) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.home_recent),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 22.dp, bottom = 4.dp),
            )
        }

        if (state.recent.isEmpty() && !state.loading) {
            item { EmptyRecent() }
        } else {
            items(state.recent, key = { it.id }) { row ->
                TxRow(row, modifier = Modifier.clickable { onEdit(row.id) }, trailingChevron = true)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }

        item { Spacer(Modifier.height(96.dp)) } // ruang untuk FAB
    }
}

@Composable
private fun HeaderSection(onSettings: () -> Unit, onAddManual: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.nav_home),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        IconButton(onClick = onAddManual) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.action_add_manual),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onSettings) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.nav_settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HeroSection(state: HomeUiState) {
    Column(modifier = Modifier.padding(top = 10.dp, bottom = 14.dp)) {
        Text(
            text = stringResource(R.string.home_total_label, state.jumlahAkun),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = formatRupiah(state.total),
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp),
        )
        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlowChip(
                color = Income,
                label = stringResource(R.string.home_in),
                value = "+${formatGrouped(state.masuk)}",
            )
            FlowChip(
                color = Expense,
                label = stringResource(R.string.home_out),
                value = "−${formatGrouped(state.keluar)}",
            )
        }
    }
}

@Composable
private fun FlowChip(color: androidx.compose.ui.graphics.Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            fontFamily = MonoFamily,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

@Composable
private fun EmptyRecent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.home_empty_title),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.home_empty_hint),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
