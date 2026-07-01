package com.taufik.managemoney.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.core.formatGrouped
import com.taufik.managemoney.core.formatRupiah
import com.taufik.managemoney.ui.components.AccountChip
import com.taufik.managemoney.ui.components.BalanceHeroCard
import com.taufik.managemoney.ui.components.BalanceHeroStat
import com.taufik.managemoney.ui.components.TxRow
import com.taufik.managemoney.ui.theme.Emerald
import com.taufik.managemoney.ui.theme.ExpenseOnDark
import com.taufik.managemoney.ui.theme.IncomeOnDark
import java.time.LocalTime
import java.time.ZoneId

private const val DEFAULT_ACCOUNT_COUNT = 4

@Composable
fun HomeScreen(
    onAccountClick: (String) -> Unit,
    onSettings: () -> Unit,
    onEdit: (String) -> Unit,
    onSeeAllRecent: () -> Unit,
    userName: String?,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var accountsExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        item { AppRow(userName = userName, onSettings = onSettings) }
        item { HeroSection(state) }

        item {
            SectionHeader(
                title = stringResource(R.string.home_accounts),
                linkText = if (state.accounts.size > DEFAULT_ACCOUNT_COUNT) {
                    if (accountsExpanded) stringResource(R.string.home_collapse)
                    else stringResource(R.string.home_show_all_count, state.accounts.size)
                } else null,
                onLink = { accountsExpanded = !accountsExpanded },
                topPadding = 18.dp,
            )
        }

        val shown = if (accountsExpanded) state.accounts else state.accounts.take(DEFAULT_ACCOUNT_COUNT)
        items(shown.chunked(2)) { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (pair.size == 1) {
                    AccountChip(
                        account = pair[0],
                        onClick = { onAccountClick(pair[0].accountId) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    pair.forEach { acc ->
                        AccountChip(
                            account = acc,
                            onClick = { onAccountClick(acc.accountId) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.home_recent),
                linkText = if (state.recent.isNotEmpty()) stringResource(R.string.home_see_all) else null,
                onLink = onSeeAllRecent,
                topPadding = 22.dp,
            )
        }

        if (state.recent.isEmpty() && !state.loading) {
            item { EmptyRecent() }
        } else {
            items(state.recent, key = { it.id }) { row ->
                TxRow(row, modifier = Modifier.clickable { onEdit(row.id) }, trailingChevron = true)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        item { Spacer(Modifier.height(96.dp)) } // ruang untuk FAB
    }
}

/** Baris atas: avatar inisial + sapaan waktu & nama + tombol pengaturan bulat. */
@Composable
private fun AppRow(userName: String?, onSettings: () -> Unit) {
    val name = userName?.trim()?.takeIf { it.isNotEmpty() }
    val hour = remember { LocalTime.now(ZoneId.of("Asia/Jakarta")).hour }
    val greetingRes = when (hour) {
        in 4..10 -> R.string.greeting_morning
        in 11..14 -> R.string.greeting_noon
        in 15..18 -> R.string.greeting_afternoon
        else -> R.string.greeting_night
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(CircleShape).background(Emerald),
            contentAlignment = Alignment.Center,
        ) {
            val initial = name?.firstOrNull()?.uppercaseChar()
            if (initial != null) {
                Text(
                    text = initial.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                text = stringResource(greetingRes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = name ?: stringResource(R.string.home_user_fallback),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable(onClick = onSettings),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.nav_settings),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Header seksi + link opsional (mis. "Lihat semua"). */
@Composable
private fun SectionHeader(
    title: String,
    linkText: String?,
    onLink: () -> Unit,
    topPadding: androidx.compose.ui.unit.Dp,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = topPadding, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        if (linkText != null) {
            Text(
                text = linkText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onLink),
            )
        }
    }
}

@Composable
private fun HeroSection(state: HomeUiState) {
    BalanceHeroCard(
        label = stringResource(R.string.home_total_label, state.jumlahAkun),
        amount = formatRupiah(state.total),
        amountColor = Color.White,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            BalanceHeroStat(
                label = stringResource(R.string.home_in),
                value = "+${formatGrouped(state.masuk)}",
                valueColor = IncomeOnDark,
            )
            BalanceHeroStat(
                label = stringResource(R.string.home_out),
                value = "−${formatGrouped(state.keluar)}",
                valueColor = ExpenseOnDark,
            )
        }
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
