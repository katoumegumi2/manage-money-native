package com.taufik.managemoney.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taufik.managemoney.core.formatShort
import com.taufik.managemoney.ui.theme.AppTheme
import com.taufik.managemoney.ui.theme.Inter
import com.taufik.managemoney.ui.theme.MonoFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/** Batas atas sumbu yang "rapi" (1/2/5 × 10^n) di atas [v]. */
internal fun niceMax(v: Long): Long {
    if (v <= 0) return 1_000_000L
    val exp = 10.0.pow(kotlin.math.floor(log10(v.toDouble())))
    val f = v / exp
    val nice = when {
        f <= 1 -> 1.0
        f <= 2 -> 2.0
        f <= 5 -> 5.0
        else -> 10.0
    }
    return (nice * exp).toLong()
}

private fun smoothPath(points: List<Offset>): Path = Path().apply {
    if (points.isEmpty()) return@apply
    moveTo(points[0].x, points[0].y)
    for (i in 1 until points.size) {
        val a = points[i - 1]
        val b = points[i]
        val mx = (a.x + b.x) / 2
        cubicTo(mx, a.y, mx, b.y, b.x, b.y)
    }
}

/** Indeks titik terdekat dari posisi x. */
private fun nearestIndex(x: Float, xs: List<Float>): Int =
    xs.indices.minByOrNull { kotlin.math.abs(xs[it] - x) } ?: 0

private fun Modifier.selectByX(count: Int, xsProvider: () -> List<Float>, onSelect: (Int) -> Unit): Modifier =
    this
        .pointerInput(count) { detectTapGestures { onSelect(nearestIndex(it.x, xsProvider())) } }
        .pointerInput(count) { detectHorizontalDragGestures { change, _ -> onSelect(nearestIndex(change.position.x, xsProvider())) } }

private fun DrawScope.gridWithLabels(
    max: Long, top: Float, bottom: Float, right: Float, gridColor: Color,
    measurer: androidx.compose.ui.text.TextMeasurer, labelStyle: TextStyle,
) {
    listOf(0L, max / 2, max).forEach { v ->
        val y = bottom - (bottom - top) * (v.toFloat() / max)
        drawLine(gridColor, Offset(0f, y), Offset(right + 4.dp.toPx(), y), strokeWidth = 1.dp.toPx())
        val txt = if (v == 0L) "0" else formatShort(v).replace(" ", "")
        val layout = measurer.measure(txt, labelStyle)
        drawText(layout, topLeft = Offset(right + 8.dp.toPx(), y - layout.size.height / 2f))
    }
}

/**
 * Area tren satu seri (Beranda): garis aksen halus + isian gradasi tipis, titik & garis bantu di bulan terpilih.
 * [labels] di bawah sumbu; sentuh/geser untuk memilih bulan.
 */
@Composable
fun TrendAreaChart(
    values: List<Long>,
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.accent,
    height: Dp = 150.dp,
) {
    val c = AppTheme.colors
    val measurer = rememberTextMeasurer()
    val axisStyle = TextStyle(fontFamily = MonoFamily, fontSize = 9.5.sp, color = c.muted)
    val xStyle = TextStyle(fontFamily = Inter, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = c.muted)
    val xStyleOn = xStyle.copy(color = c.ink, fontWeight = FontWeight.ExtraBold)
    val xsRef = androidx.compose.runtime.remember { arrayOf(emptyList<Float>()) }
    val progress by animateFloatAsState(1f, tween(700), label = "trendIn")
    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
            .selectByX(values.size, { xsRef[0] }, onSelect),
    ) {
        if (values.isEmpty()) return@Canvas
        val max = niceMax(values.max())
        val padR = 36.dp.toPx(); val top = 12.dp.toPx(); val bottom = size.height - 22.dp.toPx()
        val right = size.width - padR
        val xs = values.indices.map { i -> 10.dp.toPx() + i * (right - 20.dp.toPx()) / (values.size - 1).coerceAtLeast(1) }
        xsRef[0] = xs
        gridWithLabels(max, top, bottom, right, c.line, measurer, axisStyle)
        val pts = values.mapIndexed { i, v -> Offset(xs[i], bottom - (bottom - top) * (v.toFloat() / max) * progress) }
        val line = smoothPath(pts)
        val area = Path().apply { addPath(line); lineTo(pts.last().x, bottom); lineTo(pts.first().x, bottom); close() }
        drawPath(area, Brush.verticalGradient(listOf(color.copy(alpha = 0.24f), color.copy(alpha = 0f)), startY = top, endY = bottom))
        drawPath(line, color, style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round))
        val s = selected.coerceIn(values.indices)
        drawLine(color.copy(alpha = 0.6f), Offset(xs[s], top), Offset(xs[s], bottom), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
        drawCircle(c.card, 6.dp.toPx(), pts[s])
        drawCircle(color, 6.dp.toPx(), pts[s], style = Stroke(2.6.dp.toPx()))
        labels.forEachIndexed { i, l ->
            val layout = measurer.measure(l, if (i == s) xStyleOn else xStyle)
            drawText(layout, topLeft = Offset(xs[i] - layout.size.width / 2f, size.height - layout.size.height))
        }
    }
}

/**
 * Arus kas: dua garis halus (masuk & keluar, satu sumbu) dengan pendar tipis, area di bawah garis keluar,
 * dan kapsul transparan menyorot bulan terpilih. Legenda/nilai ditampilkan pemanggil (baris readout).
 */
@Composable
fun FlowLinesChart(
    masuk: List<Long>,
    keluar: List<Long>,
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 176.dp,
) {
    val c = AppTheme.colors
    val measurer = rememberTextMeasurer()
    val axisStyle = TextStyle(fontFamily = MonoFamily, fontSize = 9.5.sp, color = c.muted)
    val xStyle = TextStyle(fontFamily = Inter, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = c.muted)
    val xStyleOn = xStyle.copy(color = c.ink, fontWeight = FontWeight.ExtraBold)
    val xsRef = androidx.compose.runtime.remember { arrayOf(emptyList<Float>()) }
    val progress by animateFloatAsState(1f, tween(700), label = "flowIn")
    val hl = if (c.isDark) Color(0x0FFFFFFF) else Color(0x0B121722)
    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
            .selectByX(masuk.size, { xsRef[0] }, onSelect),
    ) {
        val n = max(masuk.size, keluar.size)
        if (n == 0) return@Canvas
        val max = niceMax(max(masuk.maxOrNull() ?: 0, keluar.maxOrNull() ?: 0))
        val padR = 36.dp.toPx(); val top = 16.dp.toPx(); val bottom = size.height - 24.dp.toPx()
        val right = size.width - padR
        val left = 18.dp.toPx()
        val xs = (0 until n).map { i -> left + i * (right - left - 12.dp.toPx()) / (n - 1).coerceAtLeast(1) }
        xsRef[0] = xs
        val cw = ((right - left) / n).coerceAtMost(46.dp.toPx())
        val s = selected.coerceIn(0, n - 1)
        drawRoundRect(hl, Offset(xs[s] - cw / 2, top - 8.dp.toPx()), Size(cw, bottom - top + 8.dp.toPx()), CornerRadius(cw / 2))
        gridWithLabels(max, top, bottom, right, c.line, measurer, axisStyle)
        fun pts(v: List<Long>) = v.mapIndexed { i, x -> Offset(xs[i], bottom - (bottom - top) * (x.toFloat() / max) * progress) }
        val pk = pts(keluar); val pm = pts(masuk)
        val lk = smoothPath(pk); val lm = smoothPath(pm)
        val area = Path().apply { addPath(lk); lineTo(pk.last().x, bottom); lineTo(pk.first().x, bottom); close() }
        drawPath(area, Brush.verticalGradient(listOf(c.expenseMark.copy(alpha = 0.22f), c.expenseMark.copy(alpha = 0f)), startY = top, endY = bottom))
        listOf(lm to c.incomeMark, lk to c.expenseMark).forEach { (p, col) ->
            drawPath(p, col.copy(alpha = 0.22f), style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round))
            drawPath(p, col, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
        }
        drawCircle(c.card, 5.dp.toPx(), pm[s]); drawCircle(c.incomeMark, 5.dp.toPx(), pm[s], style = Stroke(2.5.dp.toPx()))
        drawCircle(c.card, 5.dp.toPx(), pk[s]); drawCircle(c.expenseMark, 5.dp.toPx(), pk[s], style = Stroke(2.5.dp.toPx()))
        labels.forEachIndexed { i, l ->
            val layout = measurer.measure(l, if (i == s) xStyleOn else xStyle)
            drawText(layout, topLeft = Offset(xs[i] - layout.size.width / 2f, size.height - layout.size.height))
        }
    }
}

/**
 * Meter setengah lingkaran: [ratio] (mis. keluar/masuk) pada skala 0–[maxRatio], tanda batas 100%.
 * Busur bergradasi aksen + pendar; nilai di tengah digambar pemanggil lewat [center].
 */
@Composable
fun MeterGauge(
    ratio: Float,
    modifier: Modifier = Modifier,
    maxRatio: Float = 1.2f,
    center: @Composable () -> Unit,
) {
    val c = AppTheme.colors
    val anim by animateFloatAsState((ratio / maxRatio).coerceIn(0f, 1f), tween(900), label = "meter")
    val measurer = rememberTextMeasurer()
    val limStyle = TextStyle(fontFamily = Inter, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = c.expense)
    Box(modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.BottomCenter) {
        Canvas(Modifier.widthIn(max = 260.dp).fillMaxWidth().height(140.dp)) {
            val sw = 16.dp.toPx()
            val r = min(size.width / 2f - sw - 18.dp.toPx(), size.height - sw - 6.dp.toPx())
            val cx = size.width / 2f; val cy = size.height - sw / 2f - 4.dp.toPx()
            val tl = Offset(cx - r, cy - r); val sz = Size(r * 2, r * 2)
            drawArc(c.line.copy(alpha = if (c.isDark) 1f else 0.9f), 180f, 180f, false, tl, sz, style = Stroke(sw, cap = StrokeCap.Round))
            val sweep = 180f * anim
            drawArc(c.accent.copy(alpha = 0.28f), 180f, sweep, false, tl, sz, style = Stroke(sw * 1.9f, cap = StrokeCap.Round))
            drawArc(
                Brush.sweepGradient(listOf(com.taufik.managemoney.ui.theme.mix(c.accent, Color.White, 0.55f), c.accent), center = Offset(cx, cy)),
                180f, sweep, false, tl, sz, style = Stroke(sw, cap = StrokeCap.Round),
            )
            val endA = PI * (1 - anim)
            drawCircle(Color.White, 6.dp.toPx(), Offset(cx + r * cos(endA).toFloat(), cy - r * sin(endA).toFloat()))
            val limA = PI * (1 - 1f / maxRatio)
            val inner = r - sw; val outer = r + sw
            drawLine(
                c.expense, Offset(cx + inner * cos(limA).toFloat(), cy - inner * sin(limA).toFloat()),
                Offset(cx + outer * cos(limA).toFloat(), cy - outer * sin(limA).toFloat()), 2.5.dp.toPx(), StrokeCap.Round,
            )
            val lay = measurer.measure("100%", limStyle)
            drawText(lay, topLeft = Offset(cx + (outer + 4.dp.toPx()) * cos(limA).toFloat(), cy - (outer + 4.dp.toPx()) * sin(limA).toFloat() - lay.size.height))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.BottomCenter)) { center() }
    }
}

/** Kapsul tersegmen (celah 2dp) untuk komposisi kategori; segmen terpilih tetap penuh, lainnya meredup. */
@Composable
fun CategoryCapsule(
    parts: List<Pair<Long, Color>>,
    highlighted: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val total = parts.sumOf { it.first }.coerceAtLeast(1L)
    Row(
        modifier
            .fillMaxWidth()
            .height(22.dp)
            .clip(CircleShape),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        parts.forEachIndexed { i, (v, col) ->
            val a by animateFloatAsState(if (highlighted == null || highlighted == i) 1f else 0.35f, label = "capA")
            Box(
                Modifier
                    .weight((v.toFloat() / total).coerceAtLeast(0.02f))
                    .fillMaxWidth()
                    .height(22.dp)
                    .background(col.copy(alpha = a))
                    .clickable { onSelect(if (highlighted == i) null else i) },
            )
        }
    }
}

/** Persentase bulat (tanpa floating untuk uang: hanya untuk tampilan rasio). */
internal fun pct(part: Long, total: Long): Int = if (total <= 0) 0 else ((part * 100.0) / total).roundToInt()

@Composable
fun ChartLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.height(10.dp).widthIn(10.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.ink)
    }
}
