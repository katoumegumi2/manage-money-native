package com.taufik.managemoney.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Gaya pill terpilih: SOLID = kontras (ink/putih), ACCENT = emerald-soft. */
enum class PillStyle { SOLID, ACCENT }

/**
 * Chip filter single-select (v3): terpilih = pill kontras/emerald-soft, tidak = latar `field`.
 * Warna mengikuti `colorScheme` sehingga otomatis benar di tema terang & gelap.
 */
@Composable
fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PillStyle = PillStyle.SOLID,
) {
    val cs = MaterialTheme.colorScheme
    val bg = when {
        !selected -> cs.surfaceContainerHigh
        style == PillStyle.ACCENT -> cs.primaryContainer
        else -> cs.inverseSurface
    }
    val fg = when {
        !selected -> cs.onSurfaceVariant
        style == PillStyle.ACCENT -> cs.onPrimaryContainer
        else -> cs.inverseOnSurface
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = fg,
        modifier = modifier
            .clip(RoundedCornerShape(99.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    )
}
