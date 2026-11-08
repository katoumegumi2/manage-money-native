package com.taufik.managemoney.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.data.settings.ThemeMode
import com.taufik.managemoney.ui.auth.AuthViewModel
import com.taufik.managemoney.ui.components.CardDivider
import com.taufik.managemoney.ui.components.GroupLabel
import com.taufik.managemoney.ui.components.SettingRow
import com.taufik.managemoney.ui.components.SmallPill
import com.taufik.managemoney.ui.components.SubScreenHeader
import com.taufik.managemoney.ui.components.appCard
import com.taufik.managemoney.ui.components.screenBackground
import com.taufik.managemoney.ui.settings.AiSettingsViewModel
import com.taufik.managemoney.ui.theme.AppTheme
import com.taufik.managemoney.ui.theme.ThemeViewModel

/** Menu Pengaturan v4: kartu profil + daftar berkelompok (Umum, Asisten AI, Data & cadangan) + Keluar. */
@Composable
fun SettingsMenuScreen(
    onBack: () -> Unit,
    onAccounts: () -> Unit,
    onAppearance: () -> Unit,
    onAi: () -> Unit,
    onData: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    aiViewModel: AiSettingsViewModel = hiltViewModel(),
) {
    val c = AppTheme.colors
    val context = LocalContext.current
    val auth by authViewModel.state.collectAsStateWithLifecycle()
    val busy by authViewModel.busy.collectAsStateWithLifecycle()
    val message by authViewModel.message.collectAsStateWithLifecycle()
    val prefs by themeViewModel.prefs.collectAsStateWithLifecycle()
    val ai by aiViewModel.state.collectAsStateWithLifecycle()
    var confirmLogout by remember { mutableStateOf(false) }

    if (message != null) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        authViewModel.messageShown()
    }

    Column(
        modifier
            .fillMaxSize()
            .screenBackground()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        SubScreenHeader(stringResource(R.string.nav_settings), onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Profil
            Row(Modifier.fillMaxWidth().appCard().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(if (c.isDark) Color(0x14FFFFFF) else c.accentSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    val initial = (auth.name ?: auth.email)?.firstOrNull()?.uppercaseChar()?.toString()
                    if (initial != null) Text(initial, style = MaterialTheme.typography.headlineSmall, color = c.accent)
                    else com.taufik.managemoney.ui.components.Sol(R.drawable.sol_user, tint = c.accent, size = 26.dp)
                }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(auth.name ?: auth.email?.substringBefore("@") ?: stringResource(R.string.home_user_fallback), style = MaterialTheme.typography.titleMedium, color = c.ink, maxLines = 1)
                    Text(
                        if (auth.signedIn) stringResource(R.string.settings_linked_drive) else stringResource(R.string.account_not_signed_in),
                        style = MaterialTheme.typography.labelMedium,
                        color = c.muted,
                        maxLines = 2,
                    )
                }
                if (auth.signedIn) {
                    SmallPill(
                        stringResource(R.string.settings_secure),
                        icon = R.drawable.sol_shield_check,
                        container = c.incomeMark.copy(alpha = if (c.isDark) 0.16f else 0.12f),
                        content = c.income,
                    )
                }
            }

            GroupLabel(stringResource(R.string.settings_group_general))
            Column(Modifier.fillMaxWidth().appCard()) {
                SettingRow(
                    R.drawable.sol_palette, stringResource(R.string.appearance_title), onAppearance,
                    subtitle = stringResource(R.string.settings_appearance_sub),
                    value = stringResource(
                        when (prefs.mode) {
                            ThemeMode.SYSTEM -> R.string.theme_system
                            ThemeMode.LIGHT -> R.string.theme_light
                            ThemeMode.DARK -> R.string.theme_dark
                        },
                    ),
                    trailing = { Box(Modifier.padding(end = 6.dp).size(12.dp).clip(CircleShape).background(c.accent)) },
                )
                CardDivider(start = 62.dp)
                SettingRow(R.drawable.sol_wallet, stringResource(R.string.settings_accounts_title), onAccounts, subtitle = stringResource(R.string.settings_accounts_desc))
            }

            GroupLabel(stringResource(R.string.nav_ask))
            Column(Modifier.fillMaxWidth().appCard()) {
                SettingRow(
                    R.drawable.sol_ai, stringResource(R.string.settings_ai_row), onAi,
                    subtitle = stringResource(R.string.settings_ai_row_sub),
                    value = ai.provider.displayName,
                )
            }

            GroupLabel(stringResource(R.string.settings_group_data))
            Column(Modifier.fillMaxWidth().appCard()) {
                SettingRow(R.drawable.sol_cloud_upload, stringResource(R.string.settings_data_row), onData, subtitle = stringResource(R.string.settings_data_row_sub))
            }

            Column(Modifier.fillMaxWidth().appCard()) {
                if (auth.signedIn) {
                    SettingRow(R.drawable.sol_logout, stringResource(R.string.account_sign_out), { if (!busy) confirmLogout = true }, danger = true, chevron = false)
                } else if (authViewModel.isLoginConfigured) {
                    SettingRow(R.drawable.sol_login, stringResource(R.string.account_sign_in), { if (!busy) authViewModel.signIn(context) }, chevron = false)
                }
            }
            Text(
                stringResource(R.string.settings_version_footer),
                style = MaterialTheme.typography.labelSmall,
                color = c.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.size(8.dp))
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text(stringResource(R.string.settings_logout_title)) },
            text = { Text(stringResource(R.string.settings_logout_body)) },
            confirmButton = {
                TextButton(onClick = { confirmLogout = false; authViewModel.signOut() }) { Text(stringResource(R.string.account_sign_out), color = c.danger) }
            },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text(stringResource(R.string.action_cancel_generic)) } },
            containerColor = if (c.isDark) Color(0xFF181B23) else Color.White,
        )
    }
}
