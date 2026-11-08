package com.taufik.managemoney.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.taufik.managemoney.R

/**
 * Tab di navbar pil (v4): Beranda · Riwayat · Laporan · Asisten + tombol + terpisah.
 * Ikon = Solar Bold Duotone. Pengaturan dibuka lewat ikon gear di Beranda.
 */
enum class TopDestination(
    val route: String,
    @StringRes val labelRes: Int,
    @DrawableRes val icon: Int,
) {
    HOME("home", R.string.nav_home, R.drawable.sol_home),
    HISTORY("history", R.string.nav_history, R.drawable.sol_history),
    REPORTS("reports", R.string.nav_reports, R.drawable.sol_chart),
    ASK_AI("ask", R.string.nav_ask, R.drawable.sol_ai),
}
