package com.taufik.managemoney.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.taufik.managemoney.R
import com.taufik.managemoney.ui.components.SubScreenHeader
import com.taufik.managemoney.ui.components.screenBackground

/** Cadangan & data: cadangan Google Drive (terenkripsi) + ekspor/impor lokal. */
@Composable
fun DataSettingsScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding()) {
        SubScreenHeader(stringResource(R.string.settings_data_row), onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BackupSection()
            DataActions()
        }
    }
}
