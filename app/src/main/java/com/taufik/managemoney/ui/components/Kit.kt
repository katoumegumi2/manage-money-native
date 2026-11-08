package com.taufik.managemoney.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.theme.AppTheme
import com.taufik.managemoney.ui.theme.mix
import com.taufik.managemoney.ui.util.categoryVisual

// =====================================================================================
// Kit komponen Redesign v4 (lihat mockup redesign-v12.html). Semua warna dari AppTheme.
// =====================================================================================

/** Ikon Solar (vector drawable `sol_*`). Duotone tetap dua lapis karena tint SrcIn menjaga alpha. */
@Composable
fun Sol(
    @DrawableRes id: Int,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 20.dp,
    contentDescription: String? = null,
) {
    Icon(painterResource(id), contentDescription = contentDescription, tint = tint, modifier = modifier.size(size))
}

/** Kartu standar: terang = putih berbayang lembut; gelap = kaca bergaris tepi tipis. */
fun Modifier.appCard(shape: Shape = RoundedCornerShape(24.dp), elevated: Boolean = true): Modifier = composed {
    val c = AppTheme.colors
    if (c.isDark) {
        this.clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0x14FFFFFF), Color(0x07FFFFFF))))
            .background(c.card.copy(alpha = 0.55f))
            .border(1.dp, c.cardBorder, shape)
    } else {
        (if (elevated) this.shadow(10.dp, shape, ambientColor = Color(0x22141E3C), spotColor = Color(0x18141E3C)) else this)
            .clip(shape)
            .background(c.card)
    }
}

/** Latar layar: terang = gradasi aksen tipis di atas; gelap = pendar aksen di pojok kanan atas. */
fun Modifier.screenBackground(): Modifier = composed {
    val c = AppTheme.colors
    if (c.isDark) {
        this.background(c.bg).drawBehind {
            drawRect(
                Brush.radialGradient(
                    colors = listOf(c.accent.copy(alpha = 0.40f), Color.Transparent),
                    center = Offset(size.width * 0.9f, -size.height * 0.04f),
                    radius = size.width * 1.05f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    colors = listOf(c.accent.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(-size.width * 0.1f, size.height * 0.7f),
                    radius = size.width * 0.9f,
                ),
            )
        }
    } else {
        this.background(c.bg).drawBehind {
            drawRect(
                Brush.verticalGradient(
                    0f to mix(c.accent, Color.White, 0.30f),
                    0.22f to mix(c.accent, c.bg, 0.10f),
                    0.40f to c.bg,
                    endY = size.height,
                ),
            )
        }
    }
}

/** Tombol ikon bulat 42dp (kembali, notifikasi, pengaturan). [badge] = titik merah. */
@Composable
fun CircleIconButton(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    badge: Boolean = false,
    tint: Color = AppTheme.colors.ink,
) {
    val c = AppTheme.colors
    Box(
        modifier = modifier
            .size(42.dp)
            .then(
                if (c.isDark) {
                    Modifier.clip(CircleShape).background(Color(0x0FFFFFFF)).border(1.dp, Color(0x1AFFFFFF), CircleShape)
                } else {
                    Modifier.shadow(6.dp, CircleShape, ambientColor = Color(0x26141E3C), spotColor = Color(0x1A141E3C)).clip(CircleShape).background(Color.White)
                },
            )
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Sol(icon, tint = tint, size = 20.dp, contentDescription = contentDescription)
        if (badge) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 11.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(c.danger),
            )
        }
    }
}

/** Pil segmented (Minggu/Bulan/…, Sistem/Terang/Gelap). */
@Composable
fun <T> SegmentedPills(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    fill: Boolean = false,
) {
    val c = AppTheme.colors
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(if (c.isDark) Color(0x0DFFFFFF) else Color(0xFFEDF0F4))
            .then(if (c.isDark) Modifier.border(1.dp, Color(0x14FFFFFF), CircleShape) else Modifier)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { (value, label) ->
            val on = value == selected
            val bg by animateColorAsState(if (on) c.chipOn else Color.Transparent, label = "segbg")
            Box(
                modifier = Modifier
                    .then(if (fill) Modifier.weight(1f) else Modifier)
                    .clip(CircleShape)
                    .background(bg)
                    .then(if (on && c.isDark) Modifier.border(1.5.dp, c.accent, CircleShape) else Modifier)
                    .clickable(role = Role.Tab) { onSelect(value) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (on) c.chipOnContent else c.chipOffContent,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Tombol utama berbentuk pil bergradasi aksen. */
@Composable
fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
    height: Dp = 50.dp,
) {
    val c = AppTheme.colors
    val alpha by animateFloatAsState(if (enabled) 1f else 0.45f, label = "btnA")
    Row(
        modifier = modifier
            .height(height)
            .then(
                if (enabled && !c.isDark) {
                    Modifier.shadow(12.dp, CircleShape, ambientColor = c.accent.copy(alpha = 0.5f), spotColor = c.accent.copy(alpha = 0.45f))
                } else {
                    Modifier
                },
            )
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(c.accent.copy(alpha = alpha), c.accentDeep.copy(alpha = alpha))))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Sol(icon, tint = c.onAccent, size = 18.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = c.onAccent, maxLines = 1)
    }
}

/** Tombol sekunder berbentuk pil (putih/kaca). */
@Composable
fun GhostPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
    height: Dp = 50.dp,
    contentColor: Color = AppTheme.colors.ink,
) {
    val c = AppTheme.colors
    Row(
        modifier = modifier
            .height(height)
            .then(
                if (c.isDark) {
                    Modifier.clip(CircleShape).background(Color(0x0FFFFFFF)).border(1.dp, Color(0x1AFFFFFF), CircleShape)
                } else {
                    Modifier.shadow(6.dp, CircleShape, ambientColor = Color(0x22141E3C), spotColor = Color(0x14141E3C)).clip(CircleShape).background(Color.White)
                },
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Sol(icon, tint = contentColor, size = 18.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = contentColor.copy(alpha = if (enabled) 1f else 0.4f), maxLines = 1)
    }
}

/** Pil kecil (chip info / aksi) — [on] memakai gaya terpilih. */
@Composable
fun SmallPill(
    text: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    on: Boolean = false,
    onClick: (() -> Unit)? = null,
    container: Color? = null,
    content: Color? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
) {
    val c = AppTheme.colors
    val bg = container ?: if (on) c.chipOn else c.chipOff
    val fg = content ?: if (on) c.chipOnContent else c.chipOffContent
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .then(if (on && c.isDark && container == null) Modifier.border(1.5.dp, c.accent, CircleShape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) Sol(icon, tint = fg, size = 14.dp)
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = fg, maxLines = 1)
    }
}

/** Judul bagian + tautan opsional di kanan. */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    val c = AppTheme.colors
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.ink, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = c.accentText,
                modifier = Modifier.clip(CircleShape).clickable(onClick = onAction).padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
    }
}

/** Label kelompok huruf kapital kecil (Pengaturan). */
@Composable
fun GroupLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colors.muted,
        modifier = modifier.padding(start = 6.dp, top = 4.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Lingkaran ikon kategori: tint mengikuti jenis (keluar/masuk/transfer) dari AppTheme. */
@Composable
fun CategoryIcon(tipe: TxType, kategori: String, modifier: Modifier = Modifier, size: Dp = 42.dp) {
    val c = AppTheme.colors
    val tint = when (tipe) {
        TxType.PENGELUARAN -> c.expenseMark
        TxType.PEMASUKAN -> c.incomeMark
        TxType.TRANSFER -> c.transfer
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(tint.copy(alpha = if (c.isDark) 0.18f else 0.13f))
            .then(if (c.isDark) Modifier.border(1.dp, Color(0x14FFFFFF), CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Sol(categoryVisual(tipe, kategori).icon, tint = tint, size = size * 0.48f)
    }
}

/** Baris pengaturan: ikon dalam ubin ber-aksen + judul/subjudul + nilai + chevron. */
@Composable
fun SettingRow(
    @DrawableRes icon: Int,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    danger: Boolean = false,
    chevron: Boolean = true,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val c = AppTheme.colors
    val iconTint = if (danger) c.danger else if (c.isDark) c.accent else c.accentText
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (danger) c.danger.copy(alpha = 0.12f) else if (c.isDark) Color(0x0FFFFFFF) else c.accentSoft)
                .then(if (c.isDark && !danger) Modifier.border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp)) else Modifier),
            contentAlignment = Alignment.Center,
        ) { Sol(icon, tint = iconTint, size = 18.dp) }
        androidx.compose.foundation.layout.Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = if (danger) c.danger else c.ink, maxLines = 1)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        trailing?.invoke(this)
        if (value != null) {
            Text(value, style = MaterialTheme.typography.labelMedium, color = c.muted, maxLines = 1)
        }
        if (chevron) {
            Spacer(Modifier.width(6.dp))
            Sol(com.taufik.managemoney.R.drawable.sol_chevron_right, tint = c.muted, size = 16.dp)
        }
    }
}

/** Garis pemisah tipis di dalam kartu. */
@Composable
fun CardDivider(modifier: Modifier = Modifier, start: Dp = 14.dp) {
    Box(modifier.fillMaxWidth().padding(start = start, end = 14.dp).height(1.dp).background(AppTheme.colors.line))
}
