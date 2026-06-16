package com.taufik.managemoney.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.taufik.managemoney.R

/**
 * Tab di bottom navigation (mockup): Beranda · Riwayat · [FAB mic] · Laporan · Akun.
 * Pengaturan tidak di sini — dibuka lewat ikon gear di Beranda.
 */
enum class TopDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    HOME("home", R.string.nav_home, Icons.Outlined.Home),
    HISTORY("history", R.string.nav_history, Icons.AutoMirrored.Outlined.ReceiptLong),
    REPORTS("reports", R.string.nav_reports, Icons.Outlined.BarChart),
    ACCOUNTS("accounts", R.string.nav_accounts, Icons.Outlined.AccountBalanceWallet),
}
