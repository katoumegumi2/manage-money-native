package com.taufik.managemoney.ui.settings

import com.taufik.managemoney.ui.components.appCard

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.taufik.managemoney.R

/** Bagian "Data & cadangan" di layar Pengaturan: ekspor CSV, cadangkan, pulihkan DB. */
@Composable
fun DataActions(
    modifier: Modifier = Modifier,
    viewModel: DataBackupViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri -> uri?.let { viewModel.exportCsv(it) { r -> toast(context, r) } } }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri -> uri?.let { viewModel.backup(it) { r -> toast(context, r) } } }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let {
            viewModel.restore(it) { r ->
                toast(context, r)
                if (r is DataResult.Restored && r.ok) restartApp(context)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        com.taufik.managemoney.ui.components.GroupLabel(stringResource(R.string.data_section))
        Column(Modifier.fillMaxWidth().appCard()) {
            com.taufik.managemoney.ui.components.SettingRow(R.drawable.sol_document, stringResource(R.string.data_export_csv), { exportLauncher.launch("transaksi.csv") })
            com.taufik.managemoney.ui.components.CardDivider(start = 62.dp)
            com.taufik.managemoney.ui.components.SettingRow(R.drawable.sol_cloud_check, stringResource(R.string.data_backup), { backupLauncher.launch("money-backup.db") })
            com.taufik.managemoney.ui.components.CardDivider(start = 62.dp)
            com.taufik.managemoney.ui.components.SettingRow(
                R.drawable.sol_restart, stringResource(R.string.data_restore), { restoreLauncher.launch(arrayOf("*/*")) },
                subtitle = stringResource(R.string.data_restore_warning),
            )
        }
    }
}

private fun toast(context: Context, result: DataResult) {
    val msg = when (result) {
        is DataResult.Exported -> if (result.ok) R.string.data_export_ok else R.string.data_fail
        is DataResult.BackedUp -> if (result.ok) R.string.data_backup_ok else R.string.data_fail
        is DataResult.Restored -> if (result.ok) R.string.data_restore_ok else R.string.data_fail
    }
    Toast.makeText(context, context.getString(msg), Toast.LENGTH_SHORT).show()
}

private fun restartApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    Runtime.getRuntime().exit(0)
}
