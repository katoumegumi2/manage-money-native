package com.taufik.managemoney.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.data.settings.FlowColorMode
import com.taufik.managemoney.data.settings.ThemeMode
import com.taufik.managemoney.ui.components.SegmentedPills
import com.taufik.managemoney.ui.components.SmallPill
import com.taufik.managemoney.ui.components.SubScreenHeader
import com.taufik.managemoney.ui.components.appCard
import com.taufik.managemoney.ui.components.screenBackground
import com.taufik.managemoney.ui.theme.AccentPresets
import com.taufik.managemoney.ui.theme.AppTheme
import com.taufik.managemoney.ui.theme.MoneyStyle
import com.taufik.managemoney.ui.theme.flowColors
import com.taufik.managemoney.ui.theme.mix
import com.taufik.managemoney.ui.theme.ThemeViewModel

/** "#RRGGBB" dari warna (tanpa alpha). */
internal fun Color.toHex(): String = "#%06X".format(toArgb() and 0xFFFFFF)

/** Parse "#RRGGBB" / "RRGGBB" → Color, null bila tak valid. */
internal fun parseHex(input: String): Color? {
    val h = input.trim().removePrefix("#")
    if (h.length != 6 || !h.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) return null
    return Color(0xFF000000 or h.toLong(16))
}

/** Hue (0–360) dari warna. */
internal fun Color.hue(): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(toArgb(), hsv)
    return hsv[0]
}

internal fun hueColor(h: Float): Color = Color(android.graphics.Color.HSVToColor(floatArrayOf(h, 0.8f, 0.96f)))

/**
 * Tampilan: mode (sistem/terang/gelap), warna aksen (preset, slider hue, kode hex), dan warna masuk/keluar.
 * Semua langsung diterapkan & disimpan (tanpa tombol Simpan).
 */
@Composable
fun AppearanceScreen(onBack: () -> Unit, themeViewModel: ThemeViewModel = hiltViewModel()) {
    val prefs by themeViewModel.prefs.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    val accent = Color(prefs.accentArgb)
    Column(
        Modifier
            .fillMaxSize()
            .screenBackground()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        SubScreenHeader(stringResource(R.string.appearance_title), onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PreviewCard()
            Section(stringResource(R.string.settings_theme)) {
                SegmentedPills(
                    options = listOf(
                        ThemeMode.SYSTEM to stringResource(R.string.theme_system),
                        ThemeMode.LIGHT to stringResource(R.string.theme_light),
                        ThemeMode.DARK to stringResource(R.string.theme_dark),
                    ),
                    selected = prefs.mode,
                    onSelect = themeViewModel::set,
                    fill = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                )
            }
            AccentSection(accent, onAccent = { themeViewModel.setAccent(it.toArgb()) })
            FlowSection(prefs.flowColor, accent, onSelect = themeViewModel::setFlowColor)
        }
    }
}

@Composable
private fun Section(title: String, trailing: (@Composable () -> Unit)? = null, content: @Composable () -> Unit) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxWidth().appCard().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = c.ink, modifier = Modifier.weight(1f))
            trailing?.invoke()
        }
        content()
    }
}

@Composable
private fun PreviewCard() {
    val c = AppTheme.colors
    val panel = if (c.isDark) {
        Brush.linearGradient(listOf(mix(c.accent, Color(0xFF11141B), 0.30f), Color(0xFF11141B)))
    } else {
        Brush.linearGradient(listOf(mix(c.accent, Color.White, 0.16f), mix(c.accent, Color.White, 0.40f)))
    }
    Column(Modifier.fillMaxWidth().appCard(RoundedCornerShape(28.dp)).padding(6.dp)) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp)).background(panel)
                .then(if (c.isDark) Modifier.border(1.dp, c.accent.copy(alpha = 0.3f), RoundedCornerShape(23.dp)) else Modifier)
                .padding(14.dp),
        ) {
            Text(stringResource(R.string.appearance_preview), style = MaterialTheme.typography.labelLarge, color = c.ink)
            Text("Rp 2.318.000", style = MoneyStyle.copy(fontSize = 26.sp), color = c.ink, modifier = Modifier.padding(top = 6.dp, bottom = 10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SmallPill("▲ +50.000", container = if (c.isDark) Color(0x14FFFFFF) else Color.White, content = c.income)
                SmallPill("▼ −38.000", container = if (c.isDark) Color(0x14FFFFFF) else Color.White, content = c.expense)
            }
        }
    }
}

@Composable
private fun AccentSection(accent: Color, onAccent: (Color) -> Unit) {
    val c = AppTheme.colors
    var hex by remember { mutableStateOf(accent.toHex()) }
    var hue by remember { mutableFloatStateOf(accent.hue()) }
    LaunchedEffect(accent) {
        if (parseHex(hex) != accent) hex = accent.toHex()
        hue = accent.hue()
    }
    Section(
        stringResource(R.string.appearance_accent),
        trailing = { SmallPill(accent.toHex(), on = true) },
    ) {
        // Preset 6×2
        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AccentPresets.chunked(6).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { col ->
                        val on = col.toArgb() == accent.toArgb()
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .then(if (on) Modifier.border(3.dp, c.card, CircleShape).border(5.dp, col, CircleShape) else Modifier)
                                .padding(if (on) 5.dp else 0.dp)
                                .clip(CircleShape)
                                .background(col)
                                .then(if (c.isDark) Modifier.border(1.dp, Color(0x24FFFFFF), CircleShape) else Modifier)
                                .clickable { onAccent(col) },
                        )
                    }
                }
            }
        }
        Text(stringResource(R.string.appearance_custom), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = c.muted, modifier = Modifier.padding(top = 16.dp))
        // Slider hue di atas pita pelangi.
        Box(Modifier.fillMaxWidth().padding(top = 6.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 10.dp).height(14.dp).clip(CircleShape)
                    .background(Brush.horizontalGradient((0..6).map { hueColor(it * 60f) })),
            )
            Slider(
                value = hue,
                onValueChange = { hue = it },
                onValueChangeFinished = { onAccent(hueColor(hue)) },
                valueRange = 0f..359f,
                colors = SliderDefaults.colors(
                    thumbColor = hueColor(hue),
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                ),
            )
        }
        Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(parseHex(hex) ?: accent))
            Spacer(Modifier.size(8.dp))
            Box(
                Modifier.weight(1f).height(42.dp).clip(RoundedCornerShape(12.dp)).background(c.field)
                    .border(1.dp, if (parseHex(hex) == null) c.danger else c.line, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = hex,
                    onValueChange = { v ->
                        val clean = v.uppercase().filter { it == '#' || it.isLetterOrDigit() }.take(7)
                        hex = clean
                        parseHex(clean)?.let(onAccent)
                    },
                    singleLine = true,
                    textStyle = MoneyStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.ink),
                    cursorBrush = SolidColor(c.accent),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (parseHex(hex) == null) {
            Text(stringResource(R.string.appearance_hex_invalid), style = MaterialTheme.typography.labelSmall, color = c.danger, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun FlowSection(mode: FlowColorMode, accent: Color, onSelect: (FlowColorMode) -> Unit) {
    val c = AppTheme.colors
    Section(stringResource(R.string.appearance_flow)) {
        Text(stringResource(R.string.appearance_flow_hint), style = MaterialTheme.typography.bodySmall, color = c.muted, modifier = Modifier.padding(top = 4.dp))
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(FlowColorMode.FOLLOW_ACCENT to R.string.appearance_flow_follow, FlowColorMode.FIXED to R.string.appearance_flow_fixed).forEach { (m, label) ->
                val f = flowColors(accent, c.isDark, m)
                val on = m == mode
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(c.field)
                        .border(1.5.dp, if (on) c.accent else Color.Transparent, RoundedCornerShape(16.dp))
                        .clickable { onSelect(m) }
                        .padding(12.dp),
                ) {
                    Text(stringResource(label), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = c.muted)
                    Text("+50.000", style = MoneyStyle.copy(fontSize = 13.sp), color = f.income, modifier = Modifier.padding(top = 6.dp))
                    Text("−38.000", style = MoneyStyle.copy(fontSize = 13.sp), color = f.expense)
                }
            }
        }
    }
}
