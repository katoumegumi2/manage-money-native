package com.taufik.managemoney.ui.backup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.ui.util.restartApp

/**
 * Gate pasca-login (gaya WA): cek apakah ada cadangan di Drive.
 * - Ada → tawarkan pulihkan (minta passphrase).
 * - Tidak ada → tawarkan aktifkan backup (set passphrase) atau lewati.
 */
@Composable
fun RestoreGateScreen(viewModel: BackupViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val remoteExists by viewModel.remoteExists.collectAsStateWithLifecycle()
    val pendingConsent by viewModel.pendingConsent.collectAsStateWithLifecycle()
    val restartNeeded by viewModel.restartNeeded.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result -> viewModel.onConsentResult(result.data) }

    LaunchedEffect(Unit) { viewModel.checkRemoteBackup() }
    LaunchedEffect(pendingConsent) {
        pendingConsent?.let { consentLauncher.launch(it); viewModel.consentLaunched() }
    }
    LaunchedEffect(restartNeeded) { if (restartNeeded) restartApp(context) }
    if (message != null) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.messageShown()
    }

    var passphrase by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (remoteExists) {
                null -> {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.restore_checking), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    // Escape bila izin Drive ditolak / gagal cek → jangan terjebak di layar ini.
                    TextButton(onClick = { viewModel.skipRestore() }, enabled = !busy) {
                        Text(stringResource(R.string.restore_skip))
                    }
                }
                true -> {
                    Text(
                        stringResource(R.string.restore_found_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.restore_found_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))
                    OutlinedTextField(
                        value = passphrase,
                        onValueChange = { passphrase = it },
                        label = { Text(stringResource(R.string.backup_passphrase)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.restore(passphrase.toCharArray()) },
                        enabled = !busy && passphrase.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    ) { Text(stringResource(R.string.restore_action)) }
                    TextButton(onClick = { viewModel.skipRestore() }, enabled = !busy) {
                        Text(stringResource(R.string.restore_skip))
                    }
                }
                false -> {
                    Text(
                        stringResource(R.string.backup_enable_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.backup_enable_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))
                    OutlinedTextField(
                        value = passphrase,
                        onValueChange = { passphrase = it },
                        label = { Text(stringResource(R.string.backup_passphrase)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.enableBackup(passphrase) },
                        enabled = !busy && passphrase.length >= 6,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    ) { Text(stringResource(R.string.backup_enable_action)) }
                    TextButton(onClick = { viewModel.skipRestore() }, enabled = !busy) {
                        Text(stringResource(R.string.restore_skip))
                    }
                }
            }
            if (busy) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    }
}
