package com.taufik.managemoney.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.core.formatGrouped
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.components.AccountAvatar
import com.taufik.managemoney.ui.components.LocalBottomBarSpace
import com.taufik.managemoney.ui.components.SmallPill
import com.taufik.managemoney.ui.components.SegmentedPills
import com.taufik.managemoney.domain.usecase.HistoryPeriod
import com.taufik.managemoney.ui.components.Sol
import com.taufik.managemoney.ui.components.TxRow
import com.taufik.managemoney.ui.components.appCard
import com.taufik.managemoney.ui.components.screenBackground
import com.taufik.managemoney.ui.theme.AppTheme
import com.taufik.managemoney.ui.theme.MoneyStyle
import com.taufik.managemoney.ui.util.accountVisual
import com.taufik.managemoney.ui.util.txTypeLabel

/** Riwayat v4: cari, filter tipe & dompet (chip berlogo), ringkasan, grup per tanggal dalam kartu. */
@Composable
fun HistoryScreen(
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val c = AppTheme.colors

    LazyColumn(
        modifier.fillMaxSize().screenBackground(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = LocalBottomBarSpace.current + 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Spacer(Modifier.statusBarsPadding()) }
        item {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.nav_history), style = MaterialTheme.typography.headlineMedium, color = c.ink, modifier = Modifier.weight(1f))
                if (!state.loading) SmallPill(stringResource(R.string.history_count, state.count), on = true)
            }
        }
        item {
            SegmentedPills(
                options = listOf(
                    HistoryPeriod.WEEK to stringResource(R.string.history_period_week),
                    HistoryPeriod.MONTH to stringResource(R.string.history_period_month),
                    HistoryPeriod.THREE_MONTHS to stringResource(R.string.history_period_3m),
                    HistoryPeriod.YEAR to stringResource(R.string.history_period_year),
                    HistoryPeriod.ALL to stringResource(R.string.history_period_all),
                ),
                selected = state.filter.period,
                onSelect = viewModel::setPeriod,
                fill = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item { SearchPill(state.filter.query, viewModel::setQuery) }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallPill(stringResource(R.string.history_all), on = state.filter.tipe == null, onClick = { viewModel.setTipe(null) }, contentPadding = chipPad)
                TxType.entries.forEach { t ->
                    SmallPill(txTypeLabel(t), on = state.filter.tipe == t, onClick = { viewModel.setTipe(if (state.filter.tipe == t) null else t) }, contentPadding = chipPad)
                }
            }
        }
        if (state.accounts.isNotEmpty()) {
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WalletChip(null, stringResource(R.string.history_account_all), null, state.filter.accountId == null) { viewModel.setAccount(null) }
                    state.accounts.forEach { acc ->
                        WalletChip(acc.kode, acc.nama, acc.jenis, state.filter.accountId == acc.id) {
                            viewModel.setAccount(if (state.filter.accountId == acc.id) null else acc.id)
                        }
                    }
                }
            }
        }
        if (!state.loading && !state.isEmpty) {
            item { SummaryStrip(state.totalMasuk, state.totalKeluar) }
        }
        if (state.isEmpty && !state.loading) {
            item { EmptyHistory(hasFilter = state.filter.copy(period = HistoryPeriod.MONTH) != HistoryFilter()) }
        } else {
            state.groups.forEach { group ->
                item(key = "g-${group.label}") {
                    Column {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(group.label, style = MaterialTheme.typography.labelLarge, color = c.ink, modifier = Modifier.weight(1f))
                            if (group.keluar > 0) Text("−" + formatGrouped(group.keluar), style = MoneyStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold), color = c.expense)
                            if (group.masuk > 0) Text(
                                "  +" + formatGrouped(group.masuk),
                                style = MoneyStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                                color = c.income,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Column(Modifier.fillMaxWidth().appCard().padding(horizontal = 14.dp, vertical = 2.dp)) {
                            group.rows.forEach { row ->
                                TxRow(row, modifier = Modifier.fillMaxWidth().clickable { onEdit(row.id) }, trailingChevron = true)
                            }
                        }
                    }
                }
            }
        }
    }
}

private val chipPad = PaddingValues(horizontal = 14.dp, vertical = 8.dp)

@Composable
private fun SearchPill(value: String, onValue: (String) -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().height(50.dp).appCard(CircleShape).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Sol(R.drawable.sol_search, tint = c.muted, size = 18.dp)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(stringResource(R.string.history_search), style = MaterialTheme.typography.bodyMedium, color = c.muted, maxLines = 1)
            BasicTextField(
                value = value,
                onValueChange = onValue,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.ink),
                cursorBrush = SolidColor(c.accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Box(Modifier.clip(CircleShape).clickable { onValue("") }.padding(4.dp)) { Sol(R.drawable.sol_close, tint = c.muted, size = 16.dp) }
        }
    }
}

@Composable
private fun WalletChip(kode: String?, name: String, jenis: com.taufik.managemoney.domain.model.AccountType?, on: Boolean, onClick: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier
            .clip(CircleShape)
            .background(if (on) c.chipOn else if (c.isDark) Color(0x0DFFFFFF) else Color.White)
            .then(if (on && c.isDark) Modifier.border(1.5.dp, c.accent, CircleShape) else if (!on && c.isDark) Modifier.border(1.dp, Color(0x14FFFFFF), CircleShape) else Modifier)
            .clickable(onClick = onClick)
            .padding(start = if (kode != null) 5.dp else 14.dp, end = 14.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (kode != null && jenis != null) {
            val v = accountVisual(kode, jenis)
            AccountAvatar(kode = kode, abbr = v.abbr, color = v.color, size = 24, circle = true)
            Spacer(Modifier.width(7.dp))
        } else {
            Spacer(Modifier.height(24.dp))
        }
        Text(name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = if (on) c.chipOnContent else c.chipOffContent, maxLines = 1)
    }
}

@Composable
private fun SummaryStrip(masuk: Long, keluar: Long) {
    val c = AppTheme.colors
    Row(Modifier.fillMaxWidth().appCard().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.home_in), style = MaterialTheme.typography.labelMedium, color = c.muted)
            Text("+" + formatGrouped(masuk), style = MoneyStyle.copy(fontSize = 15.sp), color = c.income)
        }
        Box(Modifier.width(1.dp).height(30.dp).background(c.line))
        Column(Modifier.weight(1f).padding(start = 16.dp)) {
            Text(stringResource(R.string.home_out), style = MaterialTheme.typography.labelMedium, color = c.muted)
            Text("−" + formatGrouped(keluar), style = MoneyStyle.copy(fontSize = 15.sp), color = c.expense)
        }
        Box(Modifier.width(1.dp).height(30.dp).background(c.line))
        Column(Modifier.weight(1f).padding(start = 16.dp)) {
            val net = masuk - keluar
            Text(stringResource(R.string.reports_net), style = MaterialTheme.typography.labelMedium, color = c.muted)
            Text((if (net < 0) "−" else "+") + formatGrouped(net), style = MoneyStyle.copy(fontSize = 15.sp), color = if (net < 0) c.expense else c.income)
        }
    }
}

@Composable
private fun EmptyHistory(hasFilter: Boolean) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxWidth().appCard().padding(vertical = 36.dp, horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Sol(if (hasFilter) R.drawable.sol_search else R.drawable.sol_document, tint = c.muted, size = 30.dp)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(if (hasFilter) R.string.history_empty_filter else R.string.history_empty), style = MaterialTheme.typography.bodyMedium, color = c.body)
    }
}
