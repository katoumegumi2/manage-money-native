package com.taufik.managemoney.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.taufik.managemoney.core.formatRupiah
import com.taufik.managemoney.ui.theme.MonoFamily

/**
 * Menampilkan nominal rupiah dengan font mono tabular, format "Rp 1.234.567".
 * Dipakai di SELURUH app untuk angka uang (konsistensi tabular).
 */
@Composable
fun AmountText(
    amount: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
) {
    Text(
        text = formatRupiah(amount),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = style.merge(
            TextStyle(fontFamily = MonoFamily, fontFeatureSettings = "tnum"),
        ),
    )
}

@Composable
fun AmountTextLarge(amount: Long, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    AmountText(
        amount = amount,
        modifier = modifier,
        style = MaterialTheme.typography.displayMedium,
        color = color,
    )
}
