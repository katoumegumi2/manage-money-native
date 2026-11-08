package com.taufik.managemoney.ui.reports

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.core.formatRupiah
import com.taufik.managemoney.core.formatShort
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.components.CategoryCapsule
import com.taufik.managemoney.ui.components.ChartLegendDot
import com.taufik.managemoney.ui.components.FlowLinesChart
import com.taufik.managemoney.ui.components.LocalBottomBarSpace
import com.taufik.managemoney.ui.components.MeterGauge
import com.taufik.managemoney.ui.components.PrimaryPillButton
import com.taufik.managemoney.ui.components.SegmentedPills
import com.taufik.managemoney.ui.components.SmallPill
import com.taufik.managemoney.domain.model.CategorySum
import com.taufik.managemoney.domain.usecase.PeriodReport
import com.taufik.managemoney.domain.usecase.ReportPeriod
import com.taufik.managemoney.ui.util.kategoriLabel
import com.taufik.managemoney.ui.components.Sol
import com.taufik.managemoney.ui.components.appCard
import com.taufik.managemoney.ui.components.screenBackground
import com.taufik.managemoney.ui.theme.AppTheme
import com.taufik.managemoney.ui.theme.MoneyStyle
import com.taufik.managemoney.ui.util.categoryVisual
import java.util.Locale

private val localeId = Locale.forLanguageTag("id-ID")

/** Satu potongan "Ke mana uangmu": maks 4 kategori terbesar + "Lainnya" (abu) agar palet tetap aman CVD. */
private data class CatSlice(val label: String, val kategori: String, val total: Long, val colorIndex: Int)

private fun slices(cats: List<CategorySum>): List<CatSlice> {
    val named = cats.filter { !it.kategori.equals("lainnya", ignoreCase = true) }.sortedByDescending { it.total }
    val top = named.take(4)
    val rest = cats.sumOf { it.total } - top.sumOf { it.total }
    return top.mapIndexed { i, b -> CatSlice(kategoriLabel(b.kategori), b.kategori.lowercase(localeId), b.total, i) } +
        if (rest > 0) listOf(CatSlice("Lainnya", "lainnya", rest, 4)) else emptyList()
}

/**
 * Laporan v4: meter keluar/masuk 6 bulan, arus kas (2 garis) dengan readout bulan terpilih,
 * "Ke mana uangmu" (kapsul tersegmen + daftar), dan insight AI.
 */
@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    LazyColumn(
        Modifier
            .fillMaxSize()
            .screenBackground(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = LocalBottomBarSpace.current + 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(Modifier.statusBarsPadding()) }
        item {
            Text(stringResource(R.string.nav_reports), style = MaterialTheme.typography.headlineMedium, color = c.ink, modifier = Modifier.padding(top = 8.dp))
        }
        item {
            SegmentedPills(
                options = listOf(
                    ReportPeriod.WEEK to stringResource(R.string.period_week),
                    ReportPeriod.MONTH to stringResource(R.string.period_month),
                    ReportPeriod.SIX_MONTHS to stringResource(R.string.period_six_months),
                    ReportPeriod.YEAR to stringResource(R.string.period_year),
                ),
                selected = state.selectedPeriod,
                onSelect = viewModel::setPeriod,
                fill = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (state.loading) {
            item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.accent) } }
            return@LazyColumn
        }
        val report = state.report
        if (report != null) {
            item { MeterCard(report) }
            item { FlowCard(report) }
            item { WhereCard(report) }
        }
        item { InsightCard(state.insight, onGenerate = viewModel::generateInsight) }
    }
}

@Composable
private fun MeterCard(report: PeriodReport) {
    val c = AppTheme.colors
    val masuk = report.masuk
    val keluar = report.keluar
    val ratio = if (masuk > 0) keluar.toFloat() / masuk else if (keluar > 0) 1.2f else 0f
    val sisa = masuk - keluar
    Column(
        Modifier.fillMaxWidth().appCard().padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.reports_meter_title), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = c.muted)
        MeterGauge(ratio = ratio, modifier = Modifier.padding(top = 6.dp)) {
            Text(
                if (masuk > 0) "${(ratio * 100).toInt()}%" else "—",
                style = MoneyStyle.copy(fontSize = 34.sp),
                color = c.ink,
            )
            Text(stringResource(R.string.reports_meter_caption), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = c.muted)
            Spacer(Modifier.height(6.dp))
        }
        Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            StatCol(stringResource(R.string.home_in), "Rp " + formatShort(masuk), c.income)
            Box(Modifier.padding(horizontal = 18.dp).width(1.dp).height(30.dp).background(c.line))
            StatCol(stringResource(R.string.home_out), "Rp " + formatShort(keluar), c.expense)
        }
        Spacer(Modifier.height(12.dp))
        val good = sisa >= 0
        SmallPill(
            stringResource(if (good) R.string.reports_left else R.string.reports_over, formatShort(kotlin.math.abs(sisa)), periodPhrase(report.period)),
            icon = if (good) R.drawable.sol_arrow_up else R.drawable.sol_arrow_down,
            container = (if (good) c.incomeMark else c.expenseMark).copy(alpha = if (c.isDark) 0.16f else 0.12f),
            content = if (good) c.income else c.expense,
        )
    }
}

private val rangeFmt = java.time.format.DateTimeFormatter.ofPattern("d MMM", localeId)
private val monthFmt = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy", localeId)

private fun periodRange(r: PeriodReport): String = when (r.period) {
    ReportPeriod.WEEK -> "${r.start.format(rangeFmt)} – ${r.end.format(rangeFmt)}"
    ReportPeriod.MONTH -> r.start.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", localeId)).replaceFirstChar { it.titlecase(localeId) }
    else -> "${r.start.format(monthFmt)} – ${r.end.format(monthFmt)}"
}

@Composable
private fun periodPhrase(p: ReportPeriod): String = stringResource(
    when (p) {
        ReportPeriod.WEEK -> R.string.period_phrase_week
        ReportPeriod.MONTH -> R.string.period_phrase_month
        ReportPeriod.SIX_MONTHS -> R.string.period_phrase_six
        ReportPeriod.YEAR -> R.string.period_phrase_year
    },
)

@Composable
private fun StatCol(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = AppTheme.colors.muted)
        Text(value, style = MoneyStyle.copy(fontSize = 15.sp), color = color, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun FlowCard(report: PeriodReport) {
    val c = AppTheme.colors
    val trend = report.buckets
    var sel by rememberSaveable(report.period) { mutableIntStateOf(trend.indexOfLast { it.current }.coerceAtLeast(0)) }
    val i = sel.coerceIn(trend.indices)
    val p = trend[i]
    val net = p.masuk - p.keluar
    Column(Modifier.fillMaxWidth().appCard().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.reports_flow), style = MaterialTheme.typography.titleMedium, color = c.ink, modifier = Modifier.weight(1f))
            ChartLegendDot(c.incomeMark, stringResource(R.string.home_in))
            Spacer(Modifier.width(12.dp))
            ChartLegendDot(c.expenseMark, stringResource(R.string.home_out))
        }
        Row(
            Modifier.padding(top = 10.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val month = p.label + if (p.current) "*" else ""
            ReadoutPill { Text(month, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = c.ink) }
            ReadoutPill { Dot(c.incomeMark); Text(formatShort(p.masuk), style = MoneyStyle.copy(fontSize = 11.5.sp), color = c.ink) }
            ReadoutPill { Dot(c.expenseMark); Text(formatShort(p.keluar), style = MoneyStyle.copy(fontSize = 11.5.sp), color = c.ink) }
            ReadoutPill {
                Text(stringResource(R.string.reports_net), style = MaterialTheme.typography.labelMedium, color = c.muted)
                Text((if (net < 0) "−" else "+") + formatShort(kotlin.math.abs(net)), style = MoneyStyle.copy(fontSize = 11.5.sp), color = if (net < 0) c.expense else c.income)
            }
        }
        FlowLinesChart(
            masuk = trend.map { it.masuk },
            keluar = trend.map { it.keluar },
            labels = trend.map { it.label },
            selected = i,
            onSelect = { sel = it },
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(stringResource(R.string.reports_flow_foot), style = MaterialTheme.typography.labelSmall, color = c.muted, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun ReadoutPill(content: @Composable () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier.clip(CircleShape).background(if (c.isDark) Color(0x0FFFFFFF) else Color(0xFFF1F3F7)).padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) { content() }
}

@Composable
private fun Dot(color: Color) = Box(Modifier.size(8.dp).clip(CircleShape).background(color))

@Composable
private fun WhereCard(report: PeriodReport) {
    val c = AppTheme.colors
    val parts = slices(report.categories)
    val total = parts.sumOf { it.total }
    var hi by rememberSaveable { mutableStateOf<Int?>(null) }
    Column(Modifier.fillMaxWidth().appCard().padding(16.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(stringResource(R.string.reports_where), style = MaterialTheme.typography.titleMedium, color = c.ink, modifier = Modifier.weight(1f))
            Text(periodRange(report), style = MaterialTheme.typography.labelMedium, color = c.muted)
        }
        if (parts.isEmpty()) {
            Text(stringResource(R.string.reports_where_empty_period), style = MaterialTheme.typography.bodySmall, color = c.muted, modifier = Modifier.padding(top = 12.dp))
            return@Column
        }
        Text(formatRupiah(total), style = MoneyStyle.copy(fontSize = 22.sp), color = c.ink, modifier = Modifier.padding(top = 6.dp, bottom = 12.dp))
        CategoryCapsule(parts.map { it.total to c.categorical[it.colorIndex] }, highlighted = hi, onSelect = { hi = it })
        Spacer(Modifier.height(10.dp))
        parts.forEachIndexed { idx, s ->
            val col = c.categorical[s.colorIndex]
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (hi == idx) (if (c.isDark) Color(0x0FFFFFFF) else Color(0x0B121722)) else Color.Transparent)
                    .clickable { hi = if (hi == idx) null else idx }
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(32.dp).clip(CircleShape).background(if (c.isDark) Color(0x0FFFFFFF) else Color(0xFFF1F3F7)), contentAlignment = Alignment.Center) {
                    Sol(categoryVisual(TxType.PENGELUARAN, s.kategori).icon, tint = col, size = 17.dp)
                }
                Text(s.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = c.ink, modifier = Modifier.weight(1f).padding(start = 10.dp))
                Text(formatRupiah(s.total), style = MoneyStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold), color = c.ink)
                Text(
                    "${if (total > 0) (s.total * 100 / total) else 0}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.muted,
                    modifier = Modifier.width(44.dp).padding(start = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun InsightCard(insight: InsightUi, onGenerate: () -> Unit) {
    val c = AppTheme.colors
    Row(Modifier.fillMaxWidth().appCard().padding(16.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .then(if (c.isDark) Modifier.border(2.dp, c.accent, CircleShape) else Modifier.background(c.accentSoft)),
            contentAlignment = Alignment.Center,
        ) { Sol(R.drawable.sol_ai, tint = c.accent, size = 20.dp) }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(stringResource(R.string.reports_insight), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = c.ink)
            when {
                insight.loading -> Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = c.accent)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.voice_processing), style = MaterialTheme.typography.bodySmall, color = c.muted)
                }
                insight.text != null -> Text(insight.text, style = MaterialTheme.typography.bodySmall, color = c.body, modifier = Modifier.padding(top = 4.dp))
                else -> {
                    insight.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = c.danger, modifier = Modifier.padding(top = 4.dp)) }
                    Text(stringResource(R.string.reports_insight_hint), style = MaterialTheme.typography.bodySmall, color = c.muted, modifier = Modifier.padding(top = 4.dp))
                    if (insight.canGenerate) {
                        PrimaryPillButton(stringResource(R.string.reports_insight_make), onGenerate, height = 40.dp, modifier = Modifier.padding(top = 10.dp))
                    }
                }
            }
        }
    }
}
