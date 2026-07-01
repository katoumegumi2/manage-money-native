package com.taufik.managemoney.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Satu potongan donut: nilai + warna. */
data class DonutSlice(val value: Long, val color: Color)

/**
 * Donut chart sederhana (Canvas): potongan proporsional + lubang tengah untuk [content].
 * Warna per potongan dari pemanggil (warna kategori).
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    diameter: Dp = 168.dp,
    ringWidth: Dp = 22.dp,
    content: @Composable () -> Unit = {},
) {
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(diameter)) {
            val total = slices.sumOf { it.value }.toFloat().takeIf { it > 0f } ?: 1f
            val stroke = ringWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val gap = if (slices.size > 1) 3f else 0f
            var start = -90f
            slices.forEach { slice ->
                val sweep = (slice.value / total) * 360f
                if (sweep > 0f) {
                    drawArc(
                        color = slice.color,
                        startAngle = start + gap / 2f,
                        sweepAngle = (sweep - gap).coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                }
                start += sweep
            }
        }
        content()
    }
}
