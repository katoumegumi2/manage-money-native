package com.taufik.managemoney.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taufik.managemoney.R
import com.taufik.managemoney.ui.model.AmountStyle
import com.taufik.managemoney.ui.model.TxRowUi
import com.taufik.managemoney.ui.theme.AppTheme
import com.taufik.managemoney.ui.theme.MoneyStyle

/**
 * Satu baris transaksi (v4): ikon kategori Solar dalam lingkaran ber-tint + judul/meta + nominal mono bertanda.
 * [trailing] opsional di bawah nominal (mis. "hari ini"); [trailingChevron] menandai baris bisa diedit.
 */
@Composable
fun TxRow(row: TxRowUi, modifier: Modifier = Modifier, trailingChevron: Boolean = false, trailing: String? = null) {
    val c = AppTheme.colors
    Row(
        modifier = modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(row.tipe, row.kategori)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = row.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = c.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = row.subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = c.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = row.amountText,
                color = amountColor(row.style),
                style = MoneyStyle.copy(fontSize = 14.sp),
                maxLines = 1,
            )
            if (trailing != null) {
                Text(trailing, style = MaterialTheme.typography.labelSmall, color = c.muted, textAlign = TextAlign.End)
            }
        }
        if (trailingChevron) {
            Sol(R.drawable.sol_chevron_right, tint = c.muted, size = 16.dp, modifier = Modifier.padding(start = 6.dp))
        }
    }
}

@Composable
internal fun amountColor(style: AmountStyle): Color {
    val c = AppTheme.colors
    return when (style) {
        AmountStyle.INCOME -> c.income
        AmountStyle.EXPENSE -> c.expense
        AmountStyle.TRANSFER -> c.transfer
    }
}
