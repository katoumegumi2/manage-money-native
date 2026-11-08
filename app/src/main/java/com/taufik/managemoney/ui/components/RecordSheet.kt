package com.taufik.managemoney.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.taufik.managemoney.R
import com.taufik.managemoney.ui.theme.AppTheme

/** Pilihan di modal Catat. */
enum class RecordMode { VOICE, PHOTO, GALLERY, TYPE, MANUAL }

/**
 * Modal "Catat transaksi" (dibuka tombol +): 4 cara catat + kotak ketik cepat.
 * [onQuickText] = kalimat langsung diproses AI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSheet(
    onDismiss: () -> Unit,
    onPick: (RecordMode) -> Unit,
    onQuickText: (String) -> Unit,
) {
    val c = AppTheme.colors
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = if (c.isDark) Color(0xFF14171E) else Color(0xFFF7F8FA),
        scrimColor = if (c.isDark) Color(0x8C000000) else Color(0x590C1018),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 10.dp)
                    .size(width = 44.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(if (c.isDark) Color(0x33FFFFFF) else Color(0xFFCDD3DC)),
            )
        },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
        ) {
            Text(stringResource(R.string.record_title), style = MaterialTheme.typography.headlineSmall, color = c.ink)
            Text(stringResource(R.string.record_subtitle), style = MaterialTheme.typography.bodySmall, color = c.muted)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RecordOption(R.drawable.sol_mic, R.string.record_voice, R.string.record_voice_sub, hot = true, Modifier.weight(1f)) { onPick(RecordMode.VOICE) }
                RecordOption(R.drawable.sol_camera, R.string.record_photo, R.string.record_photo_sub, hot = false, Modifier.weight(1f)) { onPick(RecordMode.PHOTO) }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RecordOption(R.drawable.sol_gallery, R.string.record_gallery, null, hot = false, Modifier.weight(1f)) { onPick(RecordMode.GALLERY) }
                RecordOption(R.drawable.sol_keyboard, R.string.record_type, null, hot = false, Modifier.weight(1f)) { onPick(RecordMode.TYPE) }
                RecordOption(R.drawable.sol_pen, R.string.record_manual, null, hot = false, Modifier.weight(1f)) { onPick(RecordMode.MANUAL) }
            }
            Spacer(Modifier.height(14.dp))
            QuickInput(onSend = onQuickText)
        }
    }
}

@Composable
private fun RecordOption(
    @DrawableRes icon: Int,
    title: Int,
    sub: Int?,
    hot: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(24.dp)
    val hotLight = hot && !c.isDark
    Column(
        modifier = modifier
            .then(
                when {
                    hotLight -> Modifier.shadow(12.dp, shape, ambientColor = c.accent.copy(alpha = 0.4f), spotColor = c.accent.copy(alpha = 0.4f))
                        .clip(shape).background(Brush.linearGradient(listOf(c.accent, c.accentDeep)))
                    c.isDark -> Modifier.clip(shape)
                        .background(Brush.verticalGradient(listOf(Color(0x12FFFFFF), Color(0x06FFFFFF))))
                        .border(1.dp, if (hot) c.accent.copy(alpha = 0.7f) else Color(0x14FFFFFF), shape)
                    else -> Modifier.shadow(8.dp, shape, ambientColor = Color(0x1A141E3C), spotColor = Color(0x14141E3C)).clip(shape).background(Color.White)
                },
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 16.dp),
    ) {
        val iconBg = when {
            hotLight -> c.onAccent.copy(alpha = 0.18f)
            c.isDark && hot -> c.accent.copy(alpha = 0.22f)
            c.isDark -> Color(0x0FFFFFFF)
            else -> c.accentSoft
        }
        val iconTint = when {
            hotLight -> c.onAccent
            c.isDark -> if (hot) Color.White else c.ink
            else -> c.accentText
        }
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBg)
                .then(if (c.isDark && hot) Modifier.border(1.5.dp, c.accent, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) { Sol(icon, tint = iconTint, size = 21.dp) }
        Spacer(Modifier.height(if (sub != null) 14.dp else 10.dp))
        Text(stringResource(title), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (hotLight) c.onAccent else c.ink, maxLines = 1)
        if (sub != null) {
            Text(
                stringResource(sub),
                style = MaterialTheme.typography.labelMedium,
                color = if (hotLight) c.onAccent.copy(alpha = 0.8f) else c.muted,
            )
        }
    }
}

@Composable
private fun QuickInput(onSend: (String) -> Unit) {
    val c = AppTheme.colors
    var text by remember { mutableStateOf("") }
    val send = { if (text.isNotBlank()) onSend(text.trim()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .then(
                if (c.isDark) {
                    Modifier.clip(CircleShape).background(Color(0x0DFFFFFF)).border(1.dp, Color(0x17FFFFFF), CircleShape)
                } else {
                    Modifier.shadow(8.dp, CircleShape, ambientColor = Color(0x1F141E3C), spotColor = Color(0x14141E3C)).clip(CircleShape).background(Color.White)
                },
            )
            .padding(start = 18.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (text.isEmpty()) {
                Text(stringResource(R.string.record_quick_hint), style = MaterialTheme.typography.bodyMedium, color = c.muted, maxLines = 1)
            }
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.ink),
                cursorBrush = SolidColor(c.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { send() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(c.accent, c.accentDeep)))
                .clickable(enabled = text.isNotBlank(), role = Role.Button) { send() },
            contentAlignment = Alignment.Center,
        ) { Sol(R.drawable.sol_send, tint = c.onAccent, size = 18.dp, contentDescription = stringResource(R.string.ask_send)) }
    }
}
