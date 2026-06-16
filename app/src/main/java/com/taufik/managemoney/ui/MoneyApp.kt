package com.taufik.managemoney.ui

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
import com.taufik.managemoney.ui.home.HomeScreen
import com.taufik.managemoney.ui.navigation.ACCOUNT_DETAIL_ROUTE
import com.taufik.managemoney.ui.navigation.ARG_ACCOUNT_ID
import com.taufik.managemoney.ui.navigation.TopDestination
import com.taufik.managemoney.ui.navigation.accountDetailRoute
import com.taufik.managemoney.ui.screens.PlaceholderScreen

@Composable
fun MoneyApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* Phase 5: buka overlay mendengarkan */ },
                icon = { Icon(Icons.Filled.Mic, contentDescription = null) },
                text = { Text(stringResource(R.string.action_voice)) },
            )
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
            composable(TopDestination.SETTINGS.route) { PlaceholderScreen(stringResource(R.string.nav_settings)) }
            composable(
                route = ACCOUNT_DETAIL_ROUTE,
                arguments = listOf(navArgument(ARG_ACCOUNT_ID) { type = NavType.LongType }),
            ) {
                AccountDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
