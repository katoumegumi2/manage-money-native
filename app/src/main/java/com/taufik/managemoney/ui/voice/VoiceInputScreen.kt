package com.taufik.managemoney.ui.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.data.stt.SttError
import com.taufik.managemoney.ui.theme.AppTheme
import com.taufik.managemoney.ui.components.CircleIconButton
import com.taufik.managemoney.ui.components.GhostPillButton
import com.taufik.managemoney.ui.components.PrimaryPillButton
import com.taufik.managemoney.ui.components.Sol
import com.taufik.managemoney.ui.components.appCard
import com.taufik.managemoney.ui.components.screenBackground
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.draw.clip

@Composable
fun VoiceInputScreen(
    onParsed: (payload: String) -> Unit,
    onType: () -> Unit,
    onManual: () -> Unit,
    onBack: () -> Unit,
    viewModel: VoiceInputViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.startListening() else viewModel.onPermissionDenied()
    }

    // Saat layar dibuka: minta izin bila perlu, lalu mulai mendengarkan.
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.startListening() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(state.payload) {
        state.payload?.let {
            onParsed(it)
            viewModel.consumePayload()
        }
    }

    val errorMessage: String? = when {
        state.sttError != null -> stringResource(sttErrorRes(state.sttError!!))
        state.parseError != null -> state.parseError
        state.emptyResult -> stringResource(R.string.voice_empty)
        else -> null
    }

    val c = AppTheme.colors
    Box(Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding()) {
        CircleIconButton(
            R.drawable.sol_close, onClick = { viewModel.cancelListening(); onBack() },
            contentDescription = stringResource(R.string.action_close),
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
        )
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            when (state.phase) {
                VoicePhase.LISTENING -> {
                    PulsingMic()
                    Text(stringResource(R.string.voice_listening), style = MaterialTheme.typography.headlineSmall, color = c.ink)
                    Box(Modifier.fillMaxWidth().appCard().padding(18.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.partial.ifBlank { stringResource(R.string.voice_hint) },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (state.partial.isBlank()) c.muted else c.ink,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Text(stringResource(R.string.voice_pause_hint), style = MaterialTheme.typography.bodySmall, color = c.muted, textAlign = TextAlign.Center)
                    PrimaryPillButton(
                        stringResource(R.string.voice_done), viewModel::finishListening,
                        enabled = state.partial.isNotBlank(), icon = R.drawable.sol_check, modifier = Modifier.fillMaxWidth(),
                    )
                    GhostPillButton(stringResource(R.string.voice_cancel), { viewModel.cancelListening(); onBack() })
                }
                VoicePhase.PROCESSING -> {
                    CircularProgressIndicator(color = c.accent)
                    Text(stringResource(R.string.voice_processing), style = MaterialTheme.typography.titleMedium, color = c.ink)
                    if (state.finalText.isNotBlank()) {
                        Box(Modifier.fillMaxWidth().appCard().padding(16.dp)) {
                            Text(stringResource(R.string.voice_you_said, state.finalText), style = MaterialTheme.typography.bodyMedium, color = c.body, textAlign = TextAlign.Center)
                        }
                    }
                }
                VoicePhase.IDLE -> {
                    Box(Modifier.size(84.dp).clip(CircleShape).background(c.accentSoft), contentAlignment = Alignment.Center) {
                        Sol(R.drawable.sol_mic, tint = c.accent, size = 38.dp)
                    }
                    if (errorMessage != null) {
                        Text(errorMessage, style = MaterialTheme.typography.bodyLarge, color = c.ink, textAlign = TextAlign.Center)
                    }
                    PrimaryPillButton(stringResource(R.string.voice_retry), { viewModel.startListening() }, icon = R.drawable.sol_mic, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GhostPillButton(stringResource(R.string.voice_type), onType, icon = R.drawable.sol_keyboard, modifier = Modifier.weight(1f))
                        GhostPillButton(stringResource(R.string.record_manual), onManual, icon = R.drawable.sol_pen, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsingMic() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "scale",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "alpha",
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .alpha(alpha)
                .background(AppTheme.colors.accent, CircleShape),
        )
        Box(
            modifier = Modifier.size(120.dp).background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(AppTheme.colors.accent, AppTheme.colors.accentDeep)), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Sol(R.drawable.sol_mic, tint = AppTheme.colors.onAccent, size = 46.dp)
        }
    }
}

private fun sttErrorRes(error: SttError): Int = when (error) {
    SttError.NO_MATCH -> R.string.stt_error_no_match
    SttError.NO_SPEECH -> R.string.stt_error_no_speech
    SttError.NETWORK -> R.string.stt_error_network
    SttError.AUDIO -> R.string.stt_error_audio
    SttError.PERMISSION -> R.string.stt_error_permission
    SttError.BUSY -> R.string.stt_error_busy
    SttError.SERVER -> R.string.stt_error_server
    SttError.UNAVAILABLE -> R.string.stt_error_unavailable
    SttError.OTHER -> R.string.stt_error_other
}
