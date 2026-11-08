package com.taufik.managemoney.ui

import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.taufik.managemoney.ui.components.LocalBottomBarSpace
import com.taufik.managemoney.ui.components.NavBarHeight
import com.taufik.managemoney.ui.components.PillNavBar
import com.taufik.managemoney.ui.components.RecordMode
import com.taufik.managemoney.ui.components.RecordSheet
import com.taufik.managemoney.ui.navigation.ARG_PARSE_PICK
import com.taufik.managemoney.ui.navigation.ARG_PARSE_TEXT
import com.taufik.managemoney.ui.navigation.PARSE_INPUT_PATTERN
import com.taufik.managemoney.ui.navigation.parseRoute
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.taufik.managemoney.ui.navigation.ASK_AI_ROUTE
import com.taufik.managemoney.ui.navigation.SETTINGS_ROUTE
import com.taufik.managemoney.ui.navigation.ACCOUNTS_ROUTE
import com.taufik.managemoney.ui.navigation.SYSTEM_SETTINGS_ROUTE
import com.taufik.managemoney.ui.navigation.APPEARANCE_ROUTE
import com.taufik.managemoney.ui.navigation.DATA_SETTINGS_ROUTE
import com.taufik.managemoney.ui.navigation.TopDestination
import com.taufik.managemoney.ui.navigation.VOICE_INPUT_ROUTE
import com.taufik.managemoney.ui.navigation.accountDetailRoute
import com.taufik.managemoney.ui.navigation.confirmRoute
import com.taufik.managemoney.ui.navigation.editTxRoute
import com.taufik.managemoney.ui.parse.ParseInputScreen
import com.taufik.managemoney.ui.reports.ReportsScreen
import com.taufik.managemoney.ui.settings.AiSettingsScreen
import com.taufik.managemoney.ui.settings.SettingsMenuScreen
import com.taufik.managemoney.ui.voice.VoiceInputScreen

@Composable
fun MoneyApp(authViewModel: com.taufik.managemoney.ui.auth.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    if (!authState.onboarded) {
        com.taufik.managemoney.ui.auth.WelcomeScreen(viewModel = authViewModel)
        return
    }
    // Pasca-login: cek sekali apakah ada cadangan di Drive (tawarkan pulihkan / aktifkan backup).
    val backupViewModel: com.taufik.managemoney.ui.backup.BackupViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val restoreChecked by backupViewModel.restoreChecked.collectAsStateWithLifecycle()
    if (authState.signedIn && !restoreChecked) {
        com.taufik.managemoney.ui.backup.RestoreGateScreen(viewModel = backupViewModel)
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val context = LocalContext.current

    // Layar input/penuh menyembunyikan bottom nav + FAB.
    val fullScreenRoutes = setOf(
        VOICE_INPUT_ROUTE, PARSE_INPUT_PATTERN, MANUAL_ENTRY_ROUTE, CONFIRM_ROUTE, EDIT_TX_ROUTE,
        SETTINGS_ROUTE, ACCOUNTS_ROUTE, SYSTEM_SETTINGS_ROUTE, APPEARANCE_ROUTE, DATA_SETTINGS_ROUTE,
    )
    // Saat keyboard muncul, sembunyikan bottom nav + FAB agar input (mis. chat AI) menempel
    // ke atas keyboard (tak ada celah setinggi navbar).
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val showChrome = currentDestination?.route !in fullScreenRoutes && !imeVisible

    fun openTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    var showRecordSheet by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    val selectedTab = TopDestination.entries.firstOrNull { dest ->
        currentDestination?.hierarchy?.any { it.route == dest.route } == true
    }
    val bottomSpace = if (showChrome) NavBarHeight + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp

    Box(Modifier.fillMaxSize()) {
      CompositionLocalProvider(LocalBottomBarSpace provides bottomSpace) {
        NavHost(
            navController = navController,
            startDestination = TopDestination.HOME.route,
            modifier = Modifier.fillMaxSize(),
            // Default Navigation = crossfade 700 ms → terasa lambat saat pindah tab cepat. Pakai fade singkat.
            enterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(150)) },
            exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(90)) },
            popEnterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(150)) },
            popExitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(90)) },
        ) {
            composable(TopDestination.HOME.route) {
                HomeScreen(
                    onAccountClick = { id -> navController.navigate(accountDetailRoute(id)) },
                    onSettings = { navController.navigate(SETTINGS_ROUTE) },
                    onEdit = { id -> navController.navigate(editTxRoute(id)) },
                    onSeeAllRecent = { openTab(TopDestination.HISTORY.route) },
                    userName = authState.name ?: authState.email?.substringBefore("@"),
                    onRecord = { showRecordSheet = true },
                    onTransfer = { navController.navigate(MANUAL_ENTRY_ROUTE) },
                    onScan = { navController.navigate(parseRoute(camera = true)) },
                )
            }
            composable(TopDestination.HISTORY.route) {
                HistoryScreen(onEdit = { id -> navController.navigate(editTxRoute(id)) })
            }
            composable(TopDestination.REPORTS.route) {
                ReportsScreen()
            }
            composable(SETTINGS_ROUTE) {
                SettingsMenuScreen(
                    onBack = { navController.popBackStack() },
                    onAccounts = { navController.navigate(ACCOUNTS_ROUTE) },
                    onAppearance = { navController.navigate(APPEARANCE_ROUTE) },
                    onAi = { navController.navigate(SYSTEM_SETTINGS_ROUTE) },
                    onData = { navController.navigate(DATA_SETTINGS_ROUTE) },
                )
            }
            composable(APPEARANCE_ROUTE) {
                com.taufik.managemoney.ui.settings.AppearanceScreen(onBack = { navController.popBackStack() })
            }
            composable(DATA_SETTINGS_ROUTE) {
                com.taufik.managemoney.ui.settings.DataSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(ACCOUNTS_ROUTE) {
                AccountsScreen(onBack = { navController.popBackStack() })
            }
            composable(SYSTEM_SETTINGS_ROUTE) {
                AiSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(ASK_AI_ROUTE) {
                com.taufik.managemoney.ui.ask.AskAiScreen(
                    userName = authState.name ?: authState.email?.substringBefore("@"),
                )
            }
            composable(
                route = ACCOUNT_DETAIL_ROUTE,
                arguments = listOf(navArgument(ARG_ACCOUNT_ID) { type = NavType.StringType }),
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
            composable(
                route = PARSE_INPUT_PATTERN,
                arguments = listOf(
                    navArgument(ARG_PARSE_TEXT) { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument(ARG_PARSE_PICK) { type = NavType.BoolType; defaultValue = false },
                    navArgument(com.taufik.managemoney.ui.navigation.ARG_PARSE_CAMERA) { type = NavType.BoolType; defaultValue = false },
                ),
            ) { entry ->
                ParseInputScreen(
                    initialText = entry.arguments?.getString(ARG_PARSE_TEXT)?.takeIf { it.isNotBlank() },
                    autoPickImage = entry.arguments?.getBoolean(ARG_PARSE_PICK) ?: false,
                    autoCamera = entry.arguments?.getBoolean(com.taufik.managemoney.ui.navigation.ARG_PARSE_CAMERA) ?: false,
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
                arguments = listOf(navArgument(ARG_TX_ID) { type = NavType.StringType }),
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
        if (showChrome) {
            PillNavBar(
                selected = selectedTab,
                onSelect = { openTab(it.route) },
                onAdd = { showRecordSheet = true },
                onAddLong = { navController.navigate(VOICE_INPUT_ROUTE) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    if (showRecordSheet) {
        RecordSheet(
            onDismiss = { showRecordSheet = false },
            onPick = { mode ->
                showRecordSheet = false
                when (mode) {
                    RecordMode.VOICE -> navController.navigate(VOICE_INPUT_ROUTE)
                    RecordMode.PHOTO -> navController.navigate(parseRoute(camera = true))
                    RecordMode.GALLERY -> navController.navigate(parseRoute(pickImage = true))
                    RecordMode.TYPE -> navController.navigate(PARSE_INPUT_ROUTE)
                    RecordMode.MANUAL -> navController.navigate(MANUAL_ENTRY_ROUTE)
                }
            },
            onQuickText = { text ->
                showRecordSheet = false
                navController.navigate(parseRoute(text = text))
            },
        )
    }
}
