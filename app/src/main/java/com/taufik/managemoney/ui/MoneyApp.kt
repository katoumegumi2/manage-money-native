package com.taufik.managemoney.ui

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.taufik.managemoney.ui.confirm.ConfirmScreen
import com.taufik.managemoney.ui.home.HomeScreen
import com.taufik.managemoney.ui.manualentry.ManualEntryScreen
import com.taufik.managemoney.ui.navigation.ACCOUNT_DETAIL_ROUTE
import com.taufik.managemoney.ui.navigation.ARG_ACCOUNT_ID
import com.taufik.managemoney.ui.navigation.ARG_CONFIRM_PAYLOAD
import com.taufik.managemoney.ui.navigation.CONFIRM_ROUTE
import com.taufik.managemoney.ui.navigation.MANUAL_ENTRY_ROUTE
import com.taufik.managemoney.ui.navigation.PARSE_INPUT_ROUTE
import com.taufik.managemoney.ui.navigation.TopDestination
import com.taufik.managemoney.ui.navigation.VOICE_INPUT_ROUTE
import com.taufik.managemoney.ui.navigation.accountDetailRoute
import com.taufik.managemoney.ui.navigation.confirmRoute
import com.taufik.managemoney.ui.parse.ParseInputScreen
import com.taufik.managemoney.ui.screens.PlaceholderScreen
import com.taufik.managemoney.ui.settings.AiSettingsScreen
import com.taufik.managemoney.ui.voice.VoiceInputScreen

@Composable
fun MoneyApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val context = LocalContext.current

    // Layar input penuh (catat/manual/konfirmasi) menyembunyikan bottom nav + FAB.
    val fullScreenRoutes = setOf(VOICE_INPUT_ROUTE, PARSE_INPUT_ROUTE, MANUAL_ENTRY_ROUTE, CONFIRM_ROUTE)
    val showChrome = currentDestination?.route !in fullScreenRoutes

    Scaffold(
        bottomBar = {
            if (showChrome) {
                NavigationBar {
                    TopDestination.entries.forEach { dest ->
                        val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = null) },
                            label = { Text(stringResource(dest.labelRes)) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showChrome) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(VOICE_INPUT_ROUTE) },
                    icon = { Icon(Icons.Filled.Mic, contentDescription = null) },
                    text = { Text(stringResource(R.string.action_voice)) },
                )
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
                )
            }
            composable(TopDestination.HISTORY.route) { PlaceholderScreen(stringResource(R.string.nav_history)) }
            composable(TopDestination.ACCOUNTS.route) { PlaceholderScreen(stringResource(R.string.nav_accounts)) }
            composable(TopDestination.SETTINGS.route) { AiSettingsScreen() }
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
