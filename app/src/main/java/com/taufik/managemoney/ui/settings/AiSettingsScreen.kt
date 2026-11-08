package com.taufik.managemoney.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.ui.components.GhostPillButton
import com.taufik.managemoney.ui.components.GroupLabel
import com.taufik.managemoney.ui.components.PrimaryPillButton
import com.taufik.managemoney.ui.components.SmallPill
import com.taufik.managemoney.ui.components.Sol
import com.taufik.managemoney.ui.components.SubScreenHeader
import com.taufik.managemoney.ui.components.appCard
import com.taufik.managemoney.ui.components.screenBackground
import com.taufik.managemoney.ui.theme.AppTheme
import com.taufik.managemoney.ui.theme.MonoFamily

/** Asisten AI v4: status, penyedia (chip), model langsung dari server (radio), API key, Tes koneksi + Simpan. */
@Composable
fun AiSettingsScreen(
    onBack: () -> Unit,
    viewModel: AiSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    var keyVisible by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding().imePadding()) {
        SubScreenHeader(stringResource(R.string.nav_ask), onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Status
            Row(Modifier.fillMaxWidth().appCard().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(42.dp).clip(CircleShape).border(2.dp, c.accent, CircleShape).background(if (c.isDark) Color.Transparent else Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Sol(R.drawable.sol_ai, tint = c.accent, size = 20.dp) }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(state.provider.displayName, style = MaterialTheme.typography.titleMedium, color = c.ink)
                    Text(state.model.ifBlank { "—" }, style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFamily), color = c.muted, maxLines = 1)
                }
                val ok = state.apiKey.isNotBlank()
                SmallPill(
                    stringResource(if (ok) R.string.ai_status_ready else R.string.ai_status_nokey),
                    icon = if (ok) R.drawable.sol_check else R.drawable.sol_warning,
                    container = (if (ok) c.incomeMark else c.warn).copy(alpha = if (c.isDark) 0.16f else 0.12f),
                    content = if (ok) c.income else c.warn,
                )
            }

            GroupLabel(stringResource(R.string.settings_provider))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.providers.forEach { p ->
                    SmallPill(
                        p.displayName,
                        on = p == state.provider,
                        onClick = { viewModel.selectProvider(p) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                    )
                }
            }

            // Model
            Column(Modifier.fillMaxWidth().appCard().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.settings_model), style = MaterialTheme.typography.titleMedium, color = c.ink, modifier = Modifier.weight(1f))
                    if (state.provider.openAiCompatible) {
                        Text(
                            stringResource(if (state.loadingModels) R.string.settings_models_loading else R.string.ai_reload_models),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = c.accentText,
                            modifier = Modifier.clip(CircleShape).clickable(enabled = state.canLoadModels, onClick = viewModel::loadModels).padding(6.dp),
                        )
                    }
                }
                val remote = state.remoteModels
                Text(
                    stringResource(if (!remote.isNullOrEmpty()) R.string.ai_models_from_server else R.string.settings_model_suggested),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.muted,
                )
                val options: List<Triple<String, String?, List<String>>> = if (!remote.isNullOrEmpty()) {
                    remote.map { m -> Triple(m.id, m.name?.takeIf { it != m.id }, if (m.supportsImage) listOf(stringResource(R.string.ai_tag_image)) else emptyList()) }
                } else {
                    state.provider.suggestedModels.map { Triple(it, null, emptyList()) }
                }
                options.forEach { (id, name, tags) ->
                    val on = id == state.model
                    Row(
                        Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(c.field)
                            .border(1.5.dp, if (on) c.accent else Color.Transparent, RoundedCornerShape(16.dp))
                            .clickable { viewModel.setModel(id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            Modifier.padding(top = 2.dp).size(20.dp).clip(CircleShape).border(2.dp, if (on) c.accent else c.muted, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { if (on) Box(Modifier.size(10.dp).clip(CircleShape).background(c.accent)) }
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(id, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = MonoFamily), fontWeight = FontWeight.SemiBold, color = c.ink)
                            if (name != null) Text(name, style = MaterialTheme.typography.labelMedium, color = c.muted)
                            if (tags.isNotEmpty()) {
                                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    tags.forEach { SmallPill(it, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 3.dp)) }
                                }
                            }
                        }
                    }
                }
                state.modelsError?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = c.danger, modifier = Modifier.padding(top = 8.dp)) }
                Spacer(Modifier.height(10.dp))
                FieldBox(
                    label = stringResource(R.string.ai_model_custom),
                    value = state.model,
                    onValue = viewModel::setModel,
                    mono = true,
                )
                if (state.showBaseUrl) {
                    Spacer(Modifier.height(10.dp))
                    FieldBox(stringResource(R.string.settings_base_url), state.baseUrl, viewModel::setBaseUrl, mono = true, keyboardType = KeyboardType.Uri)
                }
            }

            // API key
            Column(Modifier.fillMaxWidth().appCard().padding(16.dp)) {
                Text(stringResource(R.string.settings_api_key), style = MaterialTheme.typography.titleMedium, color = c.ink)
                Spacer(Modifier.height(10.dp))
                FieldBox(
                    label = null,
                    value = state.apiKey,
                    onValue = viewModel::setApiKey,
                    mono = true,
                    keyboardType = KeyboardType.Password,
                    visual = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailing = {
                        Box(Modifier.clip(CircleShape).clickable { keyVisible = !keyVisible }.padding(6.dp)) {
                            Sol(
                                if (keyVisible) R.drawable.sol_eye_closed else R.drawable.sol_eye, tint = c.muted, size = 18.dp,
                                contentDescription = stringResource(if (keyVisible) R.string.settings_hide_key else R.string.settings_show_key),
                            )
                        }
                    },
                )
                Text(stringResource(R.string.settings_key_hint), style = MaterialTheme.typography.labelMedium, color = c.muted, modifier = Modifier.padding(top = 8.dp))
            }

            state.testOutcome?.let { outcome ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background((if (outcome.ok) c.incomeMark else c.danger).copy(alpha = 0.12f)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Sol(if (outcome.ok) R.drawable.sol_check else R.drawable.sol_warning, tint = if (outcome.ok) c.income else c.danger, size = 18.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(outcome.message, style = MaterialTheme.typography.bodySmall, color = if (outcome.ok) c.income else c.danger)
                }
            }
        }
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (state.testing) {
                Box(Modifier.height(50.dp).width(130.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = c.accent)
                }
            } else {
                GhostPillButton(stringResource(R.string.settings_test), viewModel::test, enabled = state.canSave)
            }
            PrimaryPillButton(
                stringResource(if (state.saved) R.string.settings_saved else R.string.settings_save),
                viewModel::save,
                enabled = state.canSave,
                icon = if (state.saved) R.drawable.sol_check else null,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FieldBox(
    label: String?,
    value: String,
    onValue: (String) -> Unit,
    mono: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    visual: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    val c = AppTheme.colors
    Column {
        if (label != null) Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = c.muted, modifier = Modifier.padding(bottom = 6.dp))
        Row(
            Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(14.dp)).background(c.field)
                .border(1.dp, c.line, RoundedCornerShape(14.dp)).padding(start = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValue,
                singleLine = true,
                visualTransformation = visual,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.ink, fontFamily = if (mono) MonoFamily else MaterialTheme.typography.bodyMedium.fontFamily, fontSize = 13.5.sp),
                cursorBrush = SolidColor(c.accent),
                modifier = Modifier.weight(1f),
            )
            trailing?.invoke()
        }
    }
}
