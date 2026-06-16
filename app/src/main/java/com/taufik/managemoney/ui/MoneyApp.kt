package com.taufik.managemoney.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.taufik.managemoney.ui.components.MicFab
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.taufik.managemoney.R
import com.taufik.managemoney.ui.account.AccountDetailScreen
import com.taufik.managemoney.ui.accounts.AccountsScreen
import com.taufik.managemoney.ui.confirm.ConfirmScreen
import com.taufik.managemoney.ui.edit.EditTransactionScreen
import com.taufik.managemoney.ui.history.HistoryScreen
import com.taufik.managemoney.ui.home.HomeScreen
import com.taufik.managemoney.ui.manualentry.ManualEntryScreen
import com.taufik.managemoney.ui.navigation.ACCOUNT_DETAIL_ROUTE
import com.taufik.managemoney.ui.navigation.ARG_ACCOUNT_ID
import com.taufik.managemoney.ui.navigation.ARG_CONFIRM_PAYLOAD
import com.taufik.managemoney.ui.navigation.ARG_TX_ID
import com.taufik.managemoney.ui.navigation.CONFIRM_ROUTE
import com.taufik.managemoney.ui.navigation.EDIT_TX_ROUTE
import com.taufik.managemoney.ui.navigation.MANUAL_ENTRY_ROUTE
import com.taufik.managemoney.ui.navigation.PARSE_INPUT_ROUTE
import com.taufik.managemoney.ui.navigation.SETTINGS_ROUTE
import com.taufik.managemoney.ui.navigation.TopDestination
import com.taufik.managemoney.ui.navigation.VOICE_INPUT_ROUTE
import com.taufik.managemoney.ui.navigation.accountDetailRoute
import com.taufik.managemoney.ui.navigation.confirmRoute
import com.taufik.managemoney.ui.navigation.editTxRoute
import com.taufik.managemoney.ui.parse.ParseInputScreen
import com.taufik.managemoney.ui.reports.ReportsScreen
import com.taufik.managemoney.ui.settings.AiSettingsScreen
import com.taufik.managemoney.ui.voice.VoiceInputScreen

@Composable
fun MoneyApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val context = LocalContext.current

    // Layar input/penuh menyembunyikan bottom nav + FAB.
    val fullScreenRoutes = setOf(
        VOICE_INPUT_ROUTE, PARSE_INPUT_ROUTE, MANUAL_ENTRY_ROUTE, CONFIRM_ROUTE, EDIT_TX_ROUTE, SETTINGS_ROUTE,
    )
    val showChrome = currentDestination?.route !in fullScreenRoutes

    fun openTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (showChrome) {
                // Bottom nav + FAB mic yang menumpuk di tepi atas bar (separuh masuk), bukan melayang.
                Box(modifier = Modifier.fillMaxWidth()) {
                    NavigationBar {
                        TopDestination.entries.forEachIndexed { index, dest ->
                            // Sisipkan celah di tengah (slot FAB mic) antara Riwayat & Laporan.
                            if (index == 2) Spacer(Modifier.weight(1f))
                            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { openTab(dest.route) },
                                icon = { Icon(dest.icon, contentDescription = null) },
                                label = { Text(stringResource(dest.labelRes)) },
                            )
                        }
                    }
                    MicFab(
                        onClick = { navController.navigate(VOICE_INPUT_ROUTE) },
                        contentDescription = stringResource(R.string.action_voice),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-28).dp),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopDestination.HOME.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopDestination.HOME.route) {
                HomeScreen(
                    onAccountClick = { id -> navController.navigate(accountDetailRoute(id)) },
                    onSettings = { navController.navigate(SETTINGS_ROUTE) },
                )
            }
            composable(TopDestination.HISTORY.route) {
                HistoryScreen(onEdit = { id -> navController.navigate(editTxRoute(id)) })
            }
            composable(TopDestination.REPORTS.route) { ReportsScreen() }
            composable(TopDestination.ACCOUNTS.route) { AccountsScreen() }
            composable(SETTINGS_ROUTE) {
                AiSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = ACCOUNT_DETAIL_ROUTE,
                arguments = listOf(navArgument(ARG_ACCOUNT_ID) { type = NavType.LongType }),
            ) {
                AccountDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(VOICE_INPUT_ROUTE) {
                VoiceInputScreen(
                    onParsed = { payload ->
                        navController.navigate(confirmRoute(payload)) {
                            popUpTo(VOICE_INPUT_ROUTE) { inclusive = true }
                        }
                    },
                    onType = {
                        navController.navigate(PARSE_INPUT_ROUTE) {
                            popUpTo(VOICE_INPUT_ROUTE) { inclusive = true }
                        }
                    },
                    onManual = {
                        navController.navigate(MANUAL_ENTRY_ROUTE) {
                            popUpTo(VOICE_INPUT_ROUTE) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(PARSE_INPUT_ROUTE) {
                ParseInputScreen(
                    onBack = { navController.popBackStack() },
                    onParsed = { payload -> navController.navigate(confirmRoute(payload)) },
                    onManual = { navController.navigate(MANUAL_ENTRY_ROUTE) },
                )
            }
            composable(MANUAL_ENTRY_ROUTE) {
                ManualEntryScreen(
                    onBack = { navController.popBackStack() },
                    onContinue = { payload -> navController.navigate(confirmRoute(payload)) },
                )
            }
            composable(
                route = EDIT_TX_ROUTE,
                arguments = listOf(navArgument(ARG_TX_ID) { type = NavType.LongType }),
            ) {
                EditTransactionScreen(onDone = { navController.popBackStack() })
            }
            composable(
                route = CONFIRM_ROUTE,
                arguments = listOf(navArgument(ARG_CONFIRM_PAYLOAD) { type = NavType.StringType }),
            ) {
                ConfirmScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { count ->
                        Toast.makeText(
                            context,
                            context.resources.getQuantityString(R.plurals.saved_count, count, count),
                            Toast.LENGTH_SHORT,
                        ).show()
                        navController.popBackStack(TopDestination.HOME.route, inclusive = false)
                    },
                )
            }
        }
    }
}
