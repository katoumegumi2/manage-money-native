package com.taufik.managemoney.ui.reports

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.taufik.managemoney.ui.theme.Expense
import com.taufik.managemoney.ui.theme.Gold
import com.taufik.managemoney.ui.theme.Income
import com.taufik.managemoney.ui.theme.MonoFamily

@Composable
fun ReportsScreen(
    onAskAi: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        // Header + periode
        Column(modifier = Modifier.padding(top = 14.dp, bottom = 14.dp)) {
            Text(
                text = stringResource(R.string.nav_reports),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (state.period.isNotBlank()) {
                Text(
                    text = state.period,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        NetHeroCard(state)
        Spacer(Modifier.height(14.dp))
        InsightCard(state.insight, onGenerate = viewModel::generateInsight)
        Spacer(Modifier.height(10.dp))
        AskAiButton(onAskAi)
        Spacer(Modifier.height(14.dp))
        CategorySection(state)
        Spacer(Modifier.height(14.dp))
        TrendSection(state)
        Spacer(Modifier.height(96.dp))
    }
}

/** Kartu hero: arus kas bersih bulan ini + chip masuk/keluar + delta vs bulan lalu. */
@Composable
private fun NetHeroCard(state: ReportsUiState) {
    val netColor = if (state.net >= 0) Income else Expense
    SectionCard {
        Text(
            text = stringResource(R.string.reports_net_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = (if (state.net >= 0) "+" else "−") + formatRupiah(kotlin.math.abs(state.net)),
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium,
            color = netColor,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FlowPill(stringResource(R.string.home_in), state.masuk, Income, Modifier.weight(1f))
            FlowPill(stringResource(R.string.home_out), state.keluar, Expense, Modifier.weight(1f))
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
                color = if (pct == 0) MaterialTheme.colorScheme.onSurfaceVariant else if (up) Expense else Income,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun FlowPill(label: String, amount: Long, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = formatRupiah(amount),
            fontFamily = MonoFamily,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/** Kartu insight AI (aksen emas). Narasi dari cache/generate; angka tetap dihitung lokal. */
@Composable
private fun InsightCard(insight: InsightUi, onGenerate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Gold.copy(alpha = 0.10f))
            .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
            Text(
                text = stringResource(R.string.reports_insight_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            if (insight.text != null && !insight.loading) {
                TextButton(onClick = onGenerate) { Text(stringResource(R.string.reports_insight_refresh)) }
            }
        }

        when {
            insight.loading -> Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Gold)
                Text(stringResource(R.string.reports_insight_generating), style = MaterialTheme.typography.bodyMedium)
            }

            insight.text != null -> InsightText(insight.text)

            else -> {
                Text(
                    text = stringResource(R.string.reports_insight_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
                )
                insight.error?.let {
                    Text(it, style = MaterialTheme.typography.labelMedium, color = Expense, modifier = Modifier.padding(bottom = 8.dp))
                }
                if (insight.canGenerate) {
                    GoldButton(
                        text = stringResource(
                            if (insight.error != null) R.string.reports_insight_retry else R.string.reports_insight_generate,
                        ),
                        onClick = onGenerate,
                    )
                }
            }
        }
    }
}

/** Render teks insight: baris pertama = ringkasan; baris "- ..." = poin berbullet. */
@Composable
private fun InsightText(text: String) {
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEachIndexed { i, line ->
            val bullet = line.startsWith("- ") || line.startsWith("• ")
            if (bullet) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("•", color = Gold, style = MaterialTheme.typography.bodyMedium)
                    Text(line.removePrefix("- ").removePrefix("• "), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Text(
                    line,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (i == 0) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun GoldButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Gold)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color(0xFF1B1306), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CategorySection(state: ReportsUiState) {
    SectionCard {
        Text(
            text = stringResource(R.string.history_recap_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (state.bars.isEmpty() && !state.loading) {
            Text(
                text = stringResource(R.string.reports_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        } else {
            val total = state.bars.sumOf { it.total }.takeIf { it > 0 } ?: 1L
            Spacer(Modifier.height(6.dp))
            state.bars.forEach { bar ->
                val pct = ((bar.total.toFloat() / total) * 100).toInt()
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(bar.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(
                            text = formatGrouped(bar.total),
                            fontFamily = MonoFamily,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "  $pct%",
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
                                .background(Expense),
                        )
                    }
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
            fontWeight = FontWeight.SemiBold,
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

/** Tombol masuk ke chat "Tanya AI". */
@Composable
private fun AskAiButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Gold)
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.ask_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(R.string.ask_entry_subtitle),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Kartu section konsisten (surface, rounded). */
@Composable
private fun SectionCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        content = content,
    )
}
