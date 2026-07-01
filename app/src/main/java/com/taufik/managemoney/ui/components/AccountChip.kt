package com.taufik.managemoney.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taufik.managemoney.core.formatRupiah
import com.taufik.managemoney.domain.model.AccountBalance
import com.taufik.managemoney.ui.util.accountVisual

/**
 * Sel akun kompak di grid Beranda (v3): monogram brand + nama + saldo, dengan grip 6-titik.
 * [dragging] = sel yang sedang ditarik (opacity turun + border aksen).
 */
@Composable
fun AccountChip(
    account: AccountBalance,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dragging: Boolean = false,
    targeted: Boolean = false,
) {
    val visual = accountVisual(account.kode, account.jenis)
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .then(if (dragging) Modifier.shadow(10.dp, shape) else Modifier)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                when {
                    dragging -> Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, shape)
                    targeted -> Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), shape)
                    else -> Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(start = 11.dp, top = 9.dp, end = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AccountAvatar(kode = account.kode, abbr = visual.abbr, color = visual.color, size = 26)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
        ) {
            Text(
                text = account.nama,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatRupiah(account.saldo),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.Filled.DragIndicator,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 2.dp),
        )
    }
}
