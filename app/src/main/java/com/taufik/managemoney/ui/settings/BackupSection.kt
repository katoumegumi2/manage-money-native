package com.taufik.managemoney.ui.settings

import com.taufik.managemoney.ui.components.appCard

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.ui.backup.BackupViewModel
import com.taufik.managemoney.ui.util.restartApp
import java.text.DateFormat
import java.util.Date

/** Bagian backup Drive di Pengaturan: backup sekarang, passphrase, pulihkan (Phase 8c). */
@Composable
fun BackupSection(viewModel: BackupViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val meta by viewModel.meta.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val pendingConsent by viewModel.pendingConsent.collectAsStateWithLifecycle()
    val restartNeeded by viewModel.restartNeeded.collectAsStateWithLifecycle()

    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result -> viewModel.onConsentResult(result.data) }

    LaunchedEffect(pendingConsent) {
        pendingConsent?.let { consentLauncher.launch(it); viewModel.consentLaunched() }
    }
    LaunchedEffect(restartNeeded) { if (restartNeeded) restartApp(context) }
    if (message != null) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.messageShown()
    }

    var showPassphrase by remember { mutableStateOf(false) }
    var showRestore by remember { mutableStateOf(false) }

    // Hanya relevan bila akun Google tertaut.
    if (!authState.signedIn) return

    val statusText = if (meta.hasBackup) {
        val when_ = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(meta.lastBackupAt))
        stringResource(R.string.backup_last, when_, meta.lastBackupSize / 1024)
    } else {
        stringResource(R.string.backup_never)
    }
    com.taufik.managemoney.ui.components.GroupLabel(stringResource(R.string.backup_section))
    Column(Modifier.fillMaxWidth().then(Modifier.appCard())) {
        com.taufik.managemoney.ui.components.SettingRow(
            R.drawable.sol_cloud_upload, stringResource(R.string.backup_now), { if (!busy) viewModel.backupNow() },
            subtitle = statusText, chevron = false,
        )
        com.taufik.managemoney.ui.components.CardDivider(start = 62.dp)
        com.taufik.managemoney.ui.components.SettingRow(
            R.drawable.sol_restart, stringResource(R.string.restore_from_drive), { if (!busy) showRestore = true },
            subtitle = stringResource(R.string.settings_restore_sub),
        )
        com.taufik.managemoney.ui.components.CardDivider(start = 62.dp)
        com.taufik.managemoney.ui.components.SettingRow(
            R.drawable.sol_key, stringResource(if (viewModel.hasPassphrase()) R.string.backup_change_passphrase else R.string.backup_set_passphrase),
            { if (!busy) showPassphrase = true },
            subtitle = stringResource(R.string.settings_passphrase_sub),
        )
    }

    if (showPassphrase) {
        PassphraseDialog(
            title = stringResource(R.string.backup_set_passphrase),
            confirmLabel = stringResource(R.string.action_save_generic),
            onConfirm = { p -> viewModel.setPassphrase(p); showPassphrase = false },
            onDismiss = { showPassphrase = false },
        )
    }
    if (showRestore) {
        PassphraseDialog(
            title = stringResource(R.string.restore_from_drive),
            confirmLabel = stringResource(R.string.restore_action),
            onConfirm = { p -> viewModel.restore(p.toCharArray()); showRestore = false },
            onDismiss = { showRestore = false },
        )
    }
}

@Composable
private fun PassphraseDialog(
    title: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(stringResource(R.string.backup_passphrase)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }, enabled = value.length >= 6) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel_generic)) } },
    )
}
