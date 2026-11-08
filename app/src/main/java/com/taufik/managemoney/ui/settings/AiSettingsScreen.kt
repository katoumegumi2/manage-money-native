package com.taufik.managemoney.ui.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.data.settings.ThemeMode
import com.taufik.managemoney.ui.theme.Expense
import com.taufik.managemoney.ui.theme.Income
import com.taufik.managemoney.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AiSettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val themeMode by themeViewModel.mode.collectAsStateWithLifecycle()
    var keyVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(stringResource(R.string.settings_system_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ThemeSection(themeMode, themeViewModel::set)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 6.dp))
            BackupSection()
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 6.dp))
            Text(
                text = stringResource(R.string.settings_ai_title),
                style = MaterialTheme.typography.titleLarge,
            )
        Text(
            text = stringResource(R.string.settings_ai_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(R.string.settings_provider),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.providers.forEach { p ->
                FilterChip(
                    selected = p == state.provider,
                    onClick = { viewModel.selectProvider(p) },
                    label = { Text(p.displayName) },
                )
            }
        }

        OutlinedTextField(
            value = state.model,
            onValueChange = viewModel::setModel,
            label = { Text(stringResource(R.string.settings_model)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Pilihan model: daftar langsung dari server (selalu terbaru) bila berhasil dimuat,
        // selain itu saran bawaan app.
        val remote = state.remoteModels
        val chips: List<Pair<String, String>> = if (!remote.isNullOrEmpty()) {
            remote.map { m ->
                val extra = listOfNotNull(m.name?.takeIf { it != m.id }, if (m.supportsImage) "gambar" else null)
                m.id to (if (extra.isEmpty()) m.id else "${m.id} · ${extra.joinToString(" · ")}")
            }
        } else {
            state.provider.suggestedModels.map { it to it }
        }
        if (chips.isNotEmpty() || state.provider.openAiCompatible) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        if (!remote.isNullOrEmpty()) R.string.settings_model_from_server else R.string.settings_model_suggested,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (state.provider.openAiCompatible) {
                    TextButton(onClick = viewModel::loadModels, enabled = state.canLoadModels) {
                        Text(
                            stringResource(
                                if (state.loadingModels) R.string.settings_models_loading else R.string.settings_models_load,
                            ),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                chips.forEach { (id, label) ->
                    FilterChip(
                        selected = id == state.model,
                        onClick = { viewModel.setModel(id) },
                        label = { Text(label) },
                    )
                }
            }
            state.modelsError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        if (state.showBaseUrl) {
            OutlinedTextField(
                value = state.baseUrl,
                onValueChange = viewModel::setBaseUrl,
                label = { Text(stringResource(R.string.settings_base_url)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        OutlinedTextField(
            value = state.apiKey,
            onValueChange = viewModel::setApiKey,
            label = { Text(stringResource(R.string.settings_api_key)) },
            singleLine = true,
            visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { keyVisible = !keyVisible }) {
                    Icon(
                        imageVector = if (keyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = stringResource(
                            if (keyVisible) R.string.settings_hide_key else R.string.settings_show_key,
                        ),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.settings_key_hint),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = viewModel::save,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(if (state.saved) R.string.settings_saved else R.string.settings_save))
        }

        OutlinedButton(
            onClick = viewModel::test,
            enabled = state.canSave && !state.testing,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.testing) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Text("  " + stringResource(R.string.settings_testing))
            } else {
                Text(stringResource(R.string.settings_test))
            }
        }

        state.testOutcome?.let { outcome ->
            Surface(
                color = if (outcome.ok) Income.copy(alpha = 0.14f) else MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = outcome.message,
                    color = if (outcome.ok) Income else Expense,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

            DataActions()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSection(mode: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Text(
        text = stringResource(R.string.settings_theme),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        val options = listOf(
            ThemeMode.SYSTEM to R.string.theme_system,
            ThemeMode.LIGHT to R.string.theme_light,
            ThemeMode.DARK to R.string.theme_dark,
        )
        options.forEachIndexed { index, (value, labelRes) ->
            SegmentedButton(
                selected = mode == value,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(stringResource(labelRes)) }
        }
    }
}
