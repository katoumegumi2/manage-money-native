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
import com.taufik.managemoney.ui.theme.Gold

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

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize().padding(28.dp)) {
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) {
                Text(stringResource(R.string.action_close))
            }

            Column(
                modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                when (state.phase) {
                    VoicePhase.LISTENING -> {
                        PulsingMic()
                        Text(
                            text = stringResource(R.string.voice_listening),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = state.partial.ifBlank { stringResource(R.string.voice_hint) },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (state.partial.isBlank()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            textAlign = TextAlign.Center,
                        )
                        OutlinedButton(onClick = { viewModel.cancelListening(); onBack() }) {
                            Text(stringResource(R.string.voice_cancel))
                        }
                    }

                    VoicePhase.PROCESSING -> {
                        CircularProgressIndicator(color = Gold)
                        Text(
                            text = stringResource(R.string.voice_processing),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (state.finalText.isNotBlank()) {
                            Text(
                                text = stringResource(R.string.voice_you_said, state.finalText),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    VoicePhase.IDLE -> {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp),
                        )
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                        }
                        Button(
                            onClick = { viewModel.startListening() },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        ) {
                            Icon(Icons.Filled.Mic, contentDescription = null)
                            Text("  " + stringResource(R.string.voice_retry))
                        }
                        OutlinedButton(onClick = onType, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.voice_type))
                        }
                        TextButton(onClick = onManual) {
                            Text(stringResource(R.string.action_add_manual))
                        }
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
                .background(Gold, CircleShape),
        )
        Box(
            modifier = Modifier.size(120.dp).background(Gold, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = null,
                tint = Color(0xFF1B1306),
                modifier = Modifier.size(46.dp),
            )
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
