package com.taufik.managemoney.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.taufik.managemoney.R

/**
 * Tab di bottom navigation (v3): Beranda · Riwayat · [FAB +] · Laporan · Asisten AI.
 * Pengaturan dibuka lewat ikon gear di Beranda; Kelola akun kini ada DI DALAM Pengaturan.
 */
enum class TopDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    HOME("home", R.string.nav_home, Icons.Outlined.Home),
    HISTORY("history", R.string.nav_history, Icons.AutoMirrored.Outlined.ReceiptLong),
    REPORTS("reports", R.string.nav_reports, Icons.Outlined.BarChart),
    ASK_AI("ask", R.string.nav_ask, Icons.Outlined.AutoAwesome),
}
