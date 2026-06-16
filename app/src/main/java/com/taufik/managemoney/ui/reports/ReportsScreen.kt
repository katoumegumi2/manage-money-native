package com.taufik.managemoney.ui.reports

import androidx.compose.foundation.background
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
import com.taufik.managemoney.ui.theme.Expense
import com.taufik.managemoney.ui.theme.Income
import com.taufik.managemoney.ui.theme.MonoFamily

@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
    ) {
        Text(
            text = stringResource(R.string.nav_reports),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 14.dp, bottom = 12.dp),
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(stringResource(R.string.home_in), state.masuk, Income, Modifier.weight(1f))
            SummaryCard(stringResource(R.string.home_out), state.keluar, Expense, Modifier.weight(1f))
        }

        Text(
            text = stringResource(R.string.reports_trend_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 22.dp, bottom = 8.dp),
        )
        if (!state.hasTrend && !state.loading) {
            Text(
                text = stringResource(R.string.reports_trend_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        } else {
            TrendChart(state.trend)
        }

        Text(
            text = stringResource(R.string.history_recap_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 22.dp, bottom = 8.dp),
        )

        if (state.bars.isEmpty() && !state.loading) {
            Text(
                text = stringResource(R.string.reports_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        } else {
            state.bars.forEach { bar ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(bar.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.28f))
                    Box(
                        modifier = Modifier
                            .weight(0.5f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(bar.fraction)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Expense),
                        )
                    }
                    Text(
                        text = formatGrouped(bar.total),
                        fontFamily = MonoFamily,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(0.27f).padding(start = 8.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(96.dp))
    }
}

@Composable
private fun TrendChart(bars: List<TrendBar>) {
    val chartHeight = 132.dp
    Column(modifier = Modifier.fillMaxWidth()) {
        // Legenda masuk/keluar.
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
            bars.forEach { bar ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
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

@Composable
private fun TrendBarColumn(fraction: Float, color: androidx.compose.ui.graphics.Color, maxHeight: androidx.compose.ui.unit.Dp) {
    // Tinggi minimum kecil agar bar bernilai 0 tetap terlihat tipis sebagai dasar.
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
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color),
        )
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SummaryCard(label: String, amount: Long, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = formatRupiah(amount),
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
