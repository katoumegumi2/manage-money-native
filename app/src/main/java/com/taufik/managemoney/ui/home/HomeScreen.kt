package com.taufik.managemoney.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.core.formatGrouped
import com.taufik.managemoney.core.formatRupiah
import com.taufik.managemoney.domain.model.AccountBalance
import com.taufik.managemoney.ui.components.AccountAvatar
import com.taufik.managemoney.ui.components.AccountReorderGrid
import com.taufik.managemoney.ui.components.CircleIconButton
import com.taufik.managemoney.ui.components.LocalBottomBarSpace
import com.taufik.managemoney.ui.components.SectionHeader
import com.taufik.managemoney.ui.components.SmallPill
import com.taufik.managemoney.ui.components.Sol
import com.taufik.managemoney.ui.components.TrendAreaChart
import com.taufik.managemoney.ui.components.TxRow
import com.taufik.managemoney.ui.components.appCard
import com.taufik.managemoney.ui.components.screenBackground
import com.taufik.managemoney.ui.theme.AppTheme
import com.taufik.managemoney.ui.theme.MoneyStyle
import com.taufik.managemoney.ui.theme.mix
import com.taufik.managemoney.ui.util.accountVisual
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeId = Locale.forLanguageTag("id-ID")
private val stripDate = DateTimeFormatter.ofPattern("d MMM yyyy", localeId)

/**
 * Beranda v4: kartu saldo berbingkai (aksi Catat/Transfer/Struk di dalam), akun bisa digeser,
 * grafik pengeluaran 6 bulan, transaksi terbaru. Urutan akun tetap bisa diatur lewat "Atur".
 */
@Composable
fun HomeScreen(
    onAccountClick: (String) -> Unit,
    onSettings: () -> Unit,
    onEdit: (String) -> Unit,
    onSeeAllRecent: () -> Unit,
    userName: String?,
    onRecord: () -> Unit,
    onTransfer: () -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    var hidden by rememberSaveable { mutableStateOf(false) }
    var reorderOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .screenBackground(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = LocalBottomBarSpace.current + 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(Modifier.statusBarsPadding()) }
        item { Greeting(userName, onSettings) }
        item {
            BalanceFrame(
                state = state,
                hidden = hidden,
                onToggleHidden = { hidden = !hidden },
                onRecord = onRecord,
                onTransfer = onTransfer,
                onScan = onScan,
            )
        }
        if (state.accounts.isNotEmpty()) {
            item {
                SectionHeader(
                    stringResource(R.string.home_accounts),
                    action = stringResource(R.string.home_reorder),
                    onAction = { reorderOpen = true },
                )
            }
            item {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 0.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.accounts, key = { it.accountId }) { acc ->
                        AccountCard(acc, hidden, onClick = { onAccountClick(acc.accountId) })
                    }
                }
            }
        }
        if (state.trend.isNotEmpty()) {
            item { SpendingCard(state) }
        }
        item {
            SectionHeader(
                stringResource(R.string.home_recent),
                action = if (state.recent.isNotEmpty()) stringResource(R.string.home_see_all) else null,
                onAction = onSeeAllRecent,
            )
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .appCard()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            ) {
                if (state.recent.isEmpty() && !state.loading) {
                    EmptyRecent()
                } else {
                    state.recent.forEach { row ->
                        TxRow(
                            row,
                            modifier = Modifier.fillMaxWidth().clickable { onEdit(row.id) },
                        )
                    }
                }
            }
        }
    }

    if (reorderOpen) {
        ReorderSheet(
            accounts = state.accounts,
            onDismiss = { reorderOpen = false },
            onClick = { id -> reorderOpen = false; onAccountClick(id) },
            onReorder = viewModel::reorder,
        )
    }
}

@Composable
private fun Greeting(userName: String?, onSettings: () -> Unit) {
    val c = AppTheme.colors
    val name = userName?.trim()?.takeIf { it.isNotEmpty() }
    val hour = remember { LocalTime.now(ZoneId.of("Asia/Jakarta")).hour }
    val greetingRes = when (hour) {
        in 4..10 -> R.string.greeting_morning
        in 11..14 -> R.string.greeting_noon
        in 15..18 -> R.string.greeting_afternoon
        else -> R.string.greeting_night
    }
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(44.dp)
                .then(if (c.isDark) Modifier.clip(CircleShape).background(Color(0x0FFFFFFF)).border(1.dp, Color(0x1FFFFFFF), CircleShape) else Modifier.appCard(CircleShape))
                .background(if (c.isDark) Color.Transparent else Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val initial = name?.firstOrNull()?.uppercaseChar()
            if (initial != null) {
                Text(initial.toString(), style = MaterialTheme.typography.titleMedium, color = c.accent)
            } else {
                Sol(R.drawable.sol_user, tint = c.accent, size = 22.dp)
            }
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(stringResource(greetingRes), style = MaterialTheme.typography.labelMedium, color = c.muted)
            Text(name ?: stringResource(R.string.home_user_fallback), style = MaterialTheme.typography.titleMedium, color = c.ink)
        }
        CircleIconButton(R.drawable.sol_settings, onSettings, stringResource(R.string.nav_settings))
    }
}

/** Kartu saldo berbingkai (pin "Finance Mobile App"): panel dalam bergradasi aksen + strip tanggal/net. */
@Composable
private fun BalanceFrame(
    state: HomeUiState,
    hidden: Boolean,
    onToggleHidden: () -> Unit,
    onRecord: () -> Unit,
    onTransfer: () -> Unit,
    onScan: () -> Unit,
) {
    val c = AppTheme.colors
    val outer = RoundedCornerShape(30.dp)
    val inner = RoundedCornerShape(25.dp)
    val panel = if (c.isDark) {
        Brush.linearGradient(listOf(mix(c.accent, Color(0xFF11141B), 0.30f), Color(0xFF11141B)))
    } else {
        Brush.linearGradient(listOf(mix(c.accent, Color.White, 0.16f), mix(c.accent, Color.White, 0.40f)))
    }
    val net = state.masuk - state.keluar
    Column(
        Modifier
            .fillMaxWidth()
            .appCard(outer)
            .padding(6.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(inner)
                .background(panel)
                .then(if (c.isDark) Modifier.border(1.dp, c.accent.copy(alpha = 0.3f), inner) else Modifier)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.home_total_short), style = MaterialTheme.typography.labelLarge, color = c.ink, modifier = Modifier.weight(1f))
                Box(Modifier.clip(CircleShape).clickable(onClick = onToggleHidden).padding(4.dp)) {
                    Sol(
                        if (hidden) R.drawable.sol_eye_closed else R.drawable.sol_eye,
                        tint = c.ink,
                        size = 18.dp,
                        contentDescription = stringResource(if (hidden) R.string.home_show_balance else R.string.home_hide_balance),
                    )
                }
                Spacer(Modifier.width(6.dp))
                SmallPill(
                    stringResource(R.string.home_account_count, state.jumlahAkun),
                    container = if (c.isDark) Color(0x1AFFFFFF) else Color(0xB3FFFFFF),
                    content = c.ink,
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp),
                )
            }
            Text(
                if (hidden) "Rp ••••••" else formatRupiah(state.total),
                style = MoneyStyle.copy(fontSize = 32.sp, letterSpacing = (-1).sp),
                color = c.ink,
                modifier = Modifier.padding(top = 8.dp, bottom = 14.dp),
                maxLines = 1,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                HeroAction(R.drawable.sol_add, R.string.home_action_record, onRecord, Modifier.weight(1f))
                HeroAction(R.drawable.sol_transfer, R.string.home_action_transfer, onTransfer, Modifier.weight(1f))
                HeroAction(R.drawable.sol_scanner, R.string.home_action_scan, onScan, Modifier.weight(1f))
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val stripStyle = MoneyStyle.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
            Text(LocalDate.now(ZoneId.of("Asia/Jakarta")).format(stripDate).uppercase(localeId), style = stripStyle, color = c.body)
            Text("  |  ", style = stripStyle, color = c.muted)
            Text(stringResource(R.string.home_net_month).uppercase(localeId) + " ", style = stripStyle, color = c.body)
            Text(
                if (hidden) "••••" else (if (net < 0) "−" else "+") + formatGrouped(net),
                style = stripStyle,
                color = if (net < 0) c.expense else c.income,
            )
        }
    }
}

@Composable
private fun HeroAction(icon: Int, label: Int, onClick: () -> Unit, modifier: Modifier) {
    val c = AppTheme.colors
    Row(
        modifier
            .height(38.dp)
            .then(
                if (c.isDark) {
                    Modifier.clip(CircleShape).background(Color(0x14FFFFFF)).border(1.dp, Color(0x1FFFFFFF), CircleShape)
                } else {
                    Modifier.appCard(CircleShape)
                },
            )
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Sol(icon, tint = if (c.isDark) c.ink else c.accentText, size = 16.dp)
        Spacer(Modifier.width(6.dp))
        Text(stringResource(label), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = c.ink, maxLines = 1)
    }
}

@Composable
private fun AccountCard(acc: AccountBalance, hidden: Boolean, onClick: () -> Unit) {
    val c = AppTheme.colors
    val visual = accountVisual(acc.kode, acc.jenis)
    Column(
        Modifier
            .widthIn(min = 138.dp)
            .appCard(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AccountAvatar(kode = acc.kode, abbr = visual.abbr, color = visual.color, size = 30, circle = true)
            Spacer(Modifier.width(8.dp))
            Text(acc.nama, style = MaterialTheme.typography.labelLarge, color = c.ink, maxLines = 1)
        }
        Text(
            if (hidden) "••••••" else formatGrouped(acc.saldo).let { if (acc.saldo < 0) "−$it" else it },
            style = MoneyStyle.copy(fontSize = 17.sp),
            color = if (acc.saldo < 0) c.expense else c.ink,
            maxLines = 1,
        )
    }
}

/** Grafik pengeluaran 6 bulan dengan readout tetap (tak menutupi judul). Default: bulan penuh terakhir. */
@Composable
private fun SpendingCard(state: HomeUiState) {
    val c = AppTheme.colors
    val trend = state.trend
    val lastFull = (trend.size - 2).coerceAtLeast(0)
    var sel by rememberSaveable(trend.size) { mutableIntStateOf(lastFull) }
    val i = sel.coerceIn(trend.indices)
    val cur = trend[i]
    val prev = trend.getOrNull(i - 1)
    val change = prev?.takeIf { it.keluar > 0 }?.let { ((cur.keluar - it.keluar) * 100 / it.keluar).toInt() }
    val isCurrentMonth = i == trend.lastIndex
    Column(
        Modifier
            .fillMaxWidth()
            .appCard()
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.home_spend_month, cur.label + if (isCurrentMonth) stringResource(R.string.home_month_running) else ""),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = c.muted,
                modifier = Modifier.weight(1f),
            )
            SmallPill(stringResource(R.string.home_six_months), on = true)
        }
        Text(formatRupiah(cur.keluar), style = MoneyStyle.copy(fontSize = 22.sp), color = c.ink, modifier = Modifier.padding(top = 2.dp))
        Text(
            when {
                change == null -> stringResource(R.string.home_spend_first)
                change > 0 -> stringResource(R.string.home_spend_up, change)
                else -> stringResource(R.string.home_spend_down, -change)
            },
            style = MaterialTheme.typography.labelMedium,
            color = c.muted,
        )
        Spacer(Modifier.height(10.dp))
        TrendAreaChart(
            values = trend.map { it.keluar },
            labels = trend.map { it.label.replaceFirstChar { ch -> ch.titlecase(localeId) } },
            selected = i,
            onSelect = { sel = it },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReorderSheet(
    accounts: List<AccountBalance>,
    onDismiss: () -> Unit,
    onClick: (String) -> Unit,
    onReorder: (List<String>) -> Unit,
) {
    val c = AppTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (c.isDark) Color(0xFF14171E) else Color(0xFFF7F8FA),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        Column(Modifier.padding(start = 18.dp, end = 18.dp, bottom = 28.dp)) {
            Text(stringResource(R.string.home_reorder_title), style = MaterialTheme.typography.headlineSmall, color = c.ink)
            Text(stringResource(R.string.home_reorder_hint), style = MaterialTheme.typography.bodySmall, color = c.muted)
            Spacer(Modifier.height(14.dp))
            AccountReorderGrid(accounts = accounts, onClick = onClick, onReorder = onReorder)
        }
    }
}

@Composable
private fun EmptyRecent() {
    val c = AppTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Sol(R.drawable.sol_document, tint = c.muted, size = 28.dp)
        Text(stringResource(R.string.home_empty_title), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = c.ink)
        Text(stringResource(R.string.home_empty_hint), style = MaterialTheme.typography.labelMedium, color = c.muted)
    }
}
