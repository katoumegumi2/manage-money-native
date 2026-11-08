package com.taufik.managemoney.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.taufik.managemoney.R
import com.taufik.managemoney.ui.navigation.TopDestination
import com.taufik.managemoney.ui.theme.AppTheme

/** Ruang bawah yang perlu disisakan layar tab agar konten tak tertutup navbar mengambang. */
val LocalBottomBarSpace = compositionLocalOf<Dp> { 0.dp }

/** Tinggi area navbar (bar 50 + jarak bawah 14 + napas) — di atas inset navigasi sistem. */
val NavBarHeight: Dp = 88.dp

/**
 * Navbar v4 (pin DailyLoop): pil abu berisi tab; tab aktif = pil putih ber-ikon aksen + label,
 * tab lain ikon saja. Tombol + bulat terpisah: hitam (terang) / aksen berpendar (gelap).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PillNavBar(
    selected: TopDestination?,
    onSelect: (TopDestination) -> Unit,
    onAdd: () -> Unit,
    onAddLong: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    Box(modifier.fillMaxWidth()) {
        // Pudaran di belakang navbar supaya konten yang di-scroll tak tembus terlihat.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(120.dp)
                .background(Brush.verticalGradient(listOf(c.bg.copy(alpha = 0f), c.bg.copy(alpha = 0.92f), c.bg))),
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 18.dp, end = 18.dp, bottom = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .then(
                        if (c.isDark) {
                            Modifier.clip(CircleShape).background(c.navPill).border(1.dp, Color(0x14FFFFFF), CircleShape)
                        } else {
                            Modifier.shadow(10.dp, CircleShape, ambientColor = Color(0x26141E3C), spotColor = Color(0x1A141E3C)).clip(CircleShape).background(c.navPill)
                        },
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TopDestination.entries.forEach { dest ->
                    val on = dest == selected
                    val weight by animateFloatAsState(if (on) 2.3f else 1f, label = "navW")
                    val bg by animateColorAsState(if (on) c.navActive else Color.Transparent, label = "navBg")
                    Row(
                        modifier = Modifier
                            .weight(weight)
                            .height(42.dp)
                            .then(if (on && !c.isDark) Modifier.shadow(4.dp, CircleShape, ambientColor = Color(0x1F121722), spotColor = Color(0x1A121722)) else Modifier)
                            .clip(CircleShape)
                            .background(bg)
                            .then(if (on && c.isDark) Modifier.border(1.dp, Color(0x1FFFFFFF), CircleShape) else Modifier)
                            .clickable(role = Role.Tab) { onSelect(dest) },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Sol(
                            dest.icon,
                            tint = if (on) c.accent else c.navIcon,
                            size = 18.dp,
                            contentDescription = if (on) null else stringResource(dest.labelRes),
                        )
                        AnimatedVisibility(on, enter = fadeIn() + expandHorizontally(), exit = fadeOut() + shrinkHorizontally()) {
                            Row {
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    stringResource(dest.labelRes),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = c.navActiveContent,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .then(
                        if (c.isDark) {
                            Modifier.shadow(18.dp, CircleShape, ambientColor = c.accent, spotColor = c.accent)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(c.accent, c.accentDeep)))
                                .border(1.5.dp, c.accent.copy(alpha = 0.6f), CircleShape)
                        } else {
                            Modifier.shadow(12.dp, CircleShape, ambientColor = Color(0x47121722), spotColor = Color(0x47121722)).clip(CircleShape).background(c.fab)
                        },
                    )
                    .combinedClickable(role = Role.Button, onClick = onAdd, onLongClick = onAddLong),
                contentAlignment = Alignment.Center,
            ) {
                Sol(R.drawable.sol_add, tint = c.fabContent, size = 22.dp, contentDescription = stringResource(R.string.action_add))
            }
        }
    }
}
