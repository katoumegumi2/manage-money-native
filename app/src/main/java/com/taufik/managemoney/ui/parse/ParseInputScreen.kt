package com.taufik.managemoney.ui.parse

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.ui.components.CircleIconButton
import com.taufik.managemoney.ui.components.GhostPillButton
import com.taufik.managemoney.ui.components.PrimaryPillButton
import com.taufik.managemoney.ui.components.Sol
import com.taufik.managemoney.ui.components.appCard
import com.taufik.managemoney.ui.components.screenBackground
import com.taufik.managemoney.ui.theme.AppTheme

/**
 * Layar "Catat transaksi" (ketik / foto struk). [initialText] (dari kotak ketik cepat modal) diproses
 * otomatis; [autoPickImage] membuka pemilih foto langsung (opsi "Foto struk" di modal).
 */
@Composable
fun ParseInputScreen(
    onBack: () -> Unit,
    onParsed: (payload: String) -> Unit,
    onManual: () -> Unit,
    initialText: String? = null,
    autoPickImage: Boolean = false,
    viewModel: ParseInputViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    // Photo picker sistem: tanpa izin storage, maks 5 gambar sekali proses.
    val pickImages = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
    ) { uris -> viewModel.processImages(uris) }
    val launchPicker = { pickImages.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    // Argumen awal hanya dipakai sekali (tidak terulang saat kembali dari Konfirmasi).
    var consumedArgs by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (consumedArgs) return@LaunchedEffect
        consumedArgs = true
        if (initialText != null) {
            viewModel.setText(initialText)
            viewModel.process()
        } else if (autoPickImage) {
            launchPicker()
        }
    }

    LaunchedEffect(state.payload) {
        state.payload?.let {
            onParsed(it)
            viewModel.consumePayload()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .screenBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CircleIconButton(R.drawable.sol_arrow_left, onBack, stringResource(R.string.action_back))
            Text(
                stringResource(R.string.parse_title),
                style = MaterialTheme.typography.titleMedium,
                color = c.ink,
                modifier = Modifier.weight(1f).padding(end = 42.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(stringResource(R.string.parse_hint), style = MaterialTheme.typography.bodyMedium, color = c.muted)
            Box(
                Modifier
                    .fillMaxWidth()
                    .appCard(RoundedCornerShape(24.dp))
                    .padding(16.dp),
            ) {
                if (state.text.isEmpty()) {
                    Text(stringResource(R.string.parse_field_label), style = MaterialTheme.typography.bodyLarge, color = c.muted)
                }
                BasicTextField(
                    value = state.text,
                    onValueChange = viewModel::setText,
                    enabled = !state.loading,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.ink),
                    cursorBrush = SolidColor(c.accent),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                )
            }
            if (state.emptyResult) {
                InfoBox(stringResource(R.string.parse_empty), error = false)
            }
            state.error?.let { InfoBox(stringResource(R.string.parse_error_suffix, it), error = true) }

            if (state.loading) {
                Row(
                    Modifier.fillMaxWidth().height(50.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = c.accent)
                    Spacer(Modifier.size(10.dp))
                    Text(stringResource(R.string.voice_processing), style = MaterialTheme.typography.labelLarge, color = c.ink)
                }
            } else {
                PrimaryPillButton(
                    stringResource(R.string.parse_process),
                    onClick = viewModel::process,
                    enabled = state.canProcess,
                    icon = R.drawable.sol_ai,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GhostPillButton(
                    stringResource(R.string.parse_image),
                    onClick = launchPicker,
                    enabled = !state.loading,
                    icon = R.drawable.sol_gallery,
                    modifier = Modifier.weight(1f),
                )
                GhostPillButton(
                    stringResource(R.string.action_add_manual),
                    onClick = onManual,
                    icon = R.drawable.sol_pen,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun InfoBox(text: String, error: Boolean) {
    val c = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (error) c.danger.copy(alpha = 0.12f) else c.field)
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Sol(if (error) R.drawable.sol_warning else R.drawable.sol_info, tint = if (error) c.danger else c.muted, size = 18.dp)
        Spacer(Modifier.size(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = if (error) c.danger else c.body)
    }
}
