package com.taufik.managemoney.ui.parse

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParseInputScreen(
    onBack: () -> Unit,
    onParsed: (payload: String) -> Unit,
    onManual: () -> Unit,
    viewModel: ParseInputViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Photo picker sistem: tanpa izin storage, maks 5 gambar sekali proses.
    val pickImages = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
    ) { uris -> viewModel.processImages(uris) }

    LaunchedEffect(state.payload) {
        state.payload?.let {
            onParsed(it)
            viewModel.consumePayload()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(stringResource(R.string.parse_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                stringResource(R.string.parse_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = state.text,
                onValueChange = viewModel::setText,
                label = { Text(stringResource(R.string.parse_field_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                enabled = !state.loading,
            )

            if (state.emptyResult) {
                InfoBox(
                    text = stringResource(R.string.parse_empty),
                    container = MaterialTheme.colorScheme.surfaceVariant,
                    onContainer = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.error?.let { msg ->
                InfoBox(
                    text = stringResource(R.string.parse_error_suffix, msg),
                    container = MaterialTheme.colorScheme.errorContainer,
                    onContainer = MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            Button(
                onClick = viewModel::process,
                enabled = state.canProcess,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.height(0.dp))
                    Text("  " + stringResource(R.string.voice_processing))
                } else {
                    Text(stringResource(R.string.parse_process))
                }
            }

            OutlinedButton(
                onClick = {
                    pickImages.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.Image, contentDescription = null)
                Text("  " + stringResource(R.string.parse_image))
            }

            OutlinedButton(onClick = onManual, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_add_manual))
            }
        }
    }
}

@Composable
private fun InfoBox(text: String, container: androidx.compose.ui.graphics.Color, onContainer: androidx.compose.ui.graphics.Color) {
    Surface(
        color = container,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            color = onContainer,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(12.dp),
        )
    }
}
