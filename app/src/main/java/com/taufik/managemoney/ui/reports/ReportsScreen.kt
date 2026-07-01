package com.taufik.managemoney.ui.reports

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.core.formatGrouped
import com.taufik.managemoney.core.formatRupiah
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.components.BalanceHeroCard
import com.taufik.managemoney.ui.components.BalanceHeroStat
import com.taufik.managemoney.ui.components.DonutChart
import com.taufik.managemoney.ui.components.DonutSlice
import com.taufik.managemoney.ui.theme.Expense
import com.taufik.managemoney.ui.theme.ExpenseOnDark
import com.taufik.managemoney.ui.theme.Income
import com.taufik.managemoney.ui.theme.IncomeOnDark
import com.taufik.managemoney.ui.theme.MutedOnDark
import com.taufik.managemoney.ui.util.categoryVisual
import java.time.LocalDate
import java.time.ZoneId

private enum class ReportTab { SUMMARY, EXPENSE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(ReportTab.SUMMARY) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
            Text(
                text = stringResource(R.string.nav_reports),
                style = MaterialTheme.typography.headlineMedium,
            )
            if (state.period.isNotBlank()) {
                Text(
                    text = state.period,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
            val options = listOf(
                ReportTab.SUMMARY to R.string.reports_tab_summary,
                ReportTab.EXPENSE to R.string.reports_tab_expense,
            )
            options.forEachIndexed { index, (value, labelRes) ->
                SegmentedButton(
                    selected = tab == value,
                    onClick = { tab = value },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                ) { Text(stringResource(labelRes)) }
            }
        }

        when (tab) {
            ReportTab.SUMMARY -> {
                NetHeroCard(state)
                Spacer(Modifier.height(14.dp))
                SummaryCategoryCard(state, onDetail = { tab = ReportTab.EXPENSE })
                Spacer(Modifier.height(14.dp))
                TrendSection(state)
            }
            ReportTab.EXPENSE -> ExpenseTab(state)
        }
        Spacer(Modifier.height(96.dp))
    }
}

/** Kartu hero: arus kas bersih bulan ini + chip masuk/keluar + delta vs bulan lalu. */
@Composable
private fun NetHeroCard(state: ReportsUiState) {
    val net = state.net
    BalanceHeroCard(
        label = stringResource(R.string.reports_net_label),
        amount = (if (net >= 0) "+" else "−") + formatRupiah(kotlin.math.abs(net)),
        amountColor = if (net >= 0) IncomeOnDark else ExpenseOnDark,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            BalanceHeroStat(stringResource(R.string.home_in), formatRupiah(state.masuk), Color.White)
            BalanceHeroStat(stringResource(R.string.home_out), formatRupiah(state.keluar), Color.White)
        }
        state.keluarDeltaPct?.let { pct ->
            val up = pct > 0
            val text = when {
                pct == 0 -> stringResource(R.string.reports_delta_same)
                up -> stringResource(R.string.reports_delta_up, pct)
                else -> stringResource(R.string.reports_delta_down, -pct)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (pct == 0) MutedOnDark else if (up) ExpenseOnDark else IncomeOnDark,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

/** Ringkasan: "Pengeluaran bulan ini" = donut + legend kategori (nama · nominal · %). */
@Composable
private fun SummaryCategoryCard(state: ReportsUiState, onDetail: () -> Unit) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.history_recap_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (state.bars.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.reports_detail),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onDetail),
                )
            }
        }
        if (state.bars.isEmpty()) {
            Text(
                text = stringResource(R.string.reports_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp),
            )
            return@SectionCard
        }
        val total = state.bars.sumOf { it.total }.takeIf { it > 0 } ?: 1L
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            DonutChart(
                slices = state.bars.map { DonutSlice(it.total, categoryVisual(TxType.PENGELUARAN, it.label).stroke) },
                diameter = 128.dp,
                ringWidth = 18.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.reports_donut_total).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatGrouped(total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                state.bars.forEach { bar ->
                    val pct = ((bar.total.toFloat() / total) * 100).toInt()
                    val color = categoryVisual(TxType.PENGELUARAN, bar.label).stroke
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(8.dp).clip(RoundedCornerShape(3.dp)).background(color))
                        Text(
                            bar.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            formatGrouped(bar.total),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "$pct%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(30.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Tab Pengeluaran: donut besar + kartu statistik + rincian kategori (bar). */
@Composable
private fun ExpenseTab(state: ReportsUiState) {
    if (state.bars.isEmpty()) {
        SectionCard {
            Text(
                text = stringResource(R.string.reports_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        }
        return
    }
    val total = state.bars.sumOf { it.total }
    SectionCard {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            DonutChart(
                slices = state.bars.map { DonutSlice(it.total, categoryVisual(TxType.PENGELUARAN, it.label).stroke) },
                diameter = 190.dp,
                ringWidth = 26.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.reports_expense_center).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatRupiah(total),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.reports_categories_count, state.bars.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    val day = remember { LocalDate.now(ZoneId.of("Asia/Jakarta")).dayOfMonth }.coerceAtLeast(1)
    val top = state.bars.maxByOrNull { it.total }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(Modifier.weight(1f), stringResource(R.string.reports_avg_per_day), formatRupiah(total / day))
        StatCard(
            Modifier.weight(1f),
            stringResource(R.string.reports_top_category),
            top?.label ?: "-",
            sub = top?.let { formatRupiah(it.total) },
        )
    }
    Spacer(Modifier.height(12.dp))
    CategoryDetailCard(state)
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: String, sub: String? = null) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(14.dp),
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (sub != null) {
            Text(sub, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Rincian kategori (bar proporsional per warna kategori). */
@Composable
private fun CategoryDetailCard(state: ReportsUiState) {
    SectionCard {
        Text(
            text = stringResource(R.string.reports_category_detail),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        val total = state.bars.sumOf { it.total }.takeIf { it > 0 } ?: 1L
        Spacer(Modifier.height(6.dp))
        state.bars.forEach { bar ->
            val pct = ((bar.total.toFloat() / total) * 100).toInt()
            val barColor = categoryVisual(TxType.PENGELUARAN, bar.label).stroke
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).clip(RoundedCornerShape(3.dp)).background(barColor))
                    Text(
                        bar.label,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                    )
                    Text(
                        formatGrouped(bar.total),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "  $pct%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(bar.fraction)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(barColor),
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendSection(state: ReportsUiState) {
    SectionCard {
        Text(
            text = stringResource(R.string.reports_trend_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        if (!state.hasTrend && !state.loading) {
            Text(
                text = stringResource(R.string.reports_trend_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        } else {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LegendDot(Income, stringResource(R.string.home_in))
                LegendDot(Expense, stringResource(R.string.home_out))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                val chartHeight = 120.dp
                state.trend.forEach { bar ->
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.height(chartHeight),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            TrendBarColumn(bar.masukFraction, Income, chartHeight)
                            TrendBarColumn(bar.keluarFraction, Expense, chartHeight)
                        }
                        Text(
                            text = bar.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendBarColumn(fraction: Float, color: Color, maxHeight: androidx.compose.ui.unit.Dp) {
    val h = (maxHeight * fraction).coerceAtLeast(3.dp)
    Box(
        modifier = Modifier
            .width(12.dp)
            .height(h)
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            .background(if (fraction <= 0f) color.copy(alpha = 0.25f) else color),
    )
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(modifier = Modifier.size(9.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Kartu section konsisten (surface-2, rounded). */
@Composable
private fun SectionCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        content = content,
    )
}
