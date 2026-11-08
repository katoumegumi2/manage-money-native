package com.taufik.managemoney.ui.ask

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.ui.components.LocalBottomBarSpace
import com.taufik.managemoney.ui.components.PrimaryPillButton
import com.taufik.managemoney.ui.components.Sol
import com.taufik.managemoney.ui.components.appCard
import com.taufik.managemoney.ui.components.screenBackground
import com.taufik.managemoney.ui.theme.AppTheme

/** Ubah markdown ringan jawaban AI (`**tebal**`) jadi teks bergaya; tanda bintang tidak ditampilkan. */
internal fun richText(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        val start = text.indexOf("**", i)
        if (start < 0) { append(text.substring(i)); break }
        val end = text.indexOf("**", start + 2)
        if (end < 0) { append(text.substring(i)); break }
        append(text.substring(i, start))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(start + 2, end)) }
        i = end + 2
    }
}

/** Asisten AI v4: sapaan + insight + contoh, gelembung chat, input pil dengan mic (STT bawaan) & kirim. */
@Composable
fun AskAiScreen(
    userName: String? = null,
    viewModel: AskAiViewModel = hiltViewModel(),
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val sending by viewModel.sending.collectAsStateWithLifecycle()
    val provider by viewModel.provider.collectAsStateWithLifecycle()
    val insight by viewModel.insight.collectAsStateWithLifecycle()
    var input by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val c = AppTheme.colors

    LaunchedEffect(messages.size, sending) {
        val count = messages.size + if (sending) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    Column(
        Modifier
            .fillMaxSize()
            .screenBackground()
            .statusBarsPadding()
            .imePadding()
            .padding(bottom = LocalBottomBarSpace.current),
    ) {
        Header(provider)
        if (messages.isEmpty()) {
            EmptyAsk(
                provider = provider,
                userName = userName,
                insight = insight,
                onGenerate = viewModel::generateInsight,
                onExample = { viewModel.send(it); input = "" },
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                itemsIndexed(messages) { _, msg -> Bubble(msg) }
                if (sending) item { ThinkingBubble() }
            }
        }
        InputBar(
            value = input,
            onValue = { input = it },
            onSend = { viewModel.send(input); input = "" },
            sending = sending,
        )
    }
}

@Composable
private fun Header(provider: String) {
    val c = AppTheme.colors
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(44.dp)
                .then(if (c.isDark) Modifier.shadow(14.dp, CircleShape, ambientColor = c.accent, spotColor = c.accent) else Modifier)
                .clip(CircleShape)
                .background(if (c.isDark) c.card else Color.White)
                .border(2.dp, c.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Sol(R.drawable.sol_ai, tint = c.accent, size = 21.dp) }
        Column(Modifier.padding(start = 12.dp)) {
            Text(stringResource(R.string.ask_assistant_title), style = MaterialTheme.typography.titleLarge, color = c.ink)
            Text(
                stringResource(R.string.ask_header_sub, provider.ifBlank { stringResource(R.string.ask_provider_fallback) }),
                style = MaterialTheme.typography.labelMedium,
                color = c.muted,
            )
        }
    }
}

@Composable
private fun EmptyAsk(
    provider: String,
    userName: String?,
    insight: AskInsight,
    onGenerate: () -> Unit,
    onExample: (String) -> Unit,
    modifier: Modifier,
) {
    val c = AppTheme.colors
    val name = userName?.trim()?.takeIf { it.isNotEmpty() }
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.widthIn(max = 320.dp).appCard(bubbleShape(false)).padding(14.dp)) {
            Text(
                if (name != null) stringResource(R.string.ask_greeting, name) else stringResource(R.string.ask_greeting_plain),
                style = MaterialTheme.typography.bodyMedium,
                color = c.ink,
            )
        }
        InsightCard(insight, onGenerate)
        Text(
            stringResource(R.string.ask_try).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = c.muted,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        )
        listOf(
            R.string.ask_example_1 to R.drawable.sol_cat_belanja,
            R.string.ask_example_2 to R.drawable.sol_chart,
            R.string.ask_example_3 to R.drawable.sol_cat_gaji,
        ).forEach { (res, icon) ->
            val text = stringResource(res)
            Row(
                Modifier.fillMaxWidth().appCard(RoundedCornerShape(20.dp)).clickable { onExample(text) }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(c.accentSoft), contentAlignment = Alignment.Center) {
                    Sol(icon, tint = if (c.isDark) c.accent else c.accentText, size = 18.dp)
                }
                Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = c.ink, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                Sol(R.drawable.sol_arrow_right, tint = c.muted, size = 16.dp)
            }
        }
        Text(stringResource(R.string.ask_disclaimer), style = MaterialTheme.typography.labelSmall, color = c.muted, modifier = Modifier.padding(4.dp, 2.dp, 4.dp, 12.dp))
    }
}

@Composable
private fun InsightCard(insight: AskInsight, onGenerate: () -> Unit) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxWidth().appCard().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Sol(R.drawable.sol_ai, tint = c.accent, size = 18.dp)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.ask_insight_title), style = MaterialTheme.typography.titleMedium, color = c.ink, modifier = Modifier.weight(1f))
            if (insight.text != null && !insight.loading) {
                Text(
                    stringResource(R.string.ask_insight_refresh),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = c.accentText,
                    modifier = Modifier.clip(CircleShape).clickable(onClick = onGenerate).padding(6.dp),
                )
            }
        }
        when {
            insight.loading -> Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = c.accent)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.ask_insight_generating), style = MaterialTheme.typography.bodySmall, color = c.muted)
            }
            insight.text != null -> Text(richText(insight.text), style = MaterialTheme.typography.bodyMedium, color = c.body, modifier = Modifier.padding(top = 8.dp))
            else -> {
                insight.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = c.danger, modifier = Modifier.padding(top = 6.dp)) }
                Text(stringResource(R.string.ask_insight_hint), style = MaterialTheme.typography.bodySmall, color = c.muted, modifier = Modifier.padding(top = 6.dp))
                if (insight.canGenerate) {
                    PrimaryPillButton(
                        stringResource(if (insight.error != null) R.string.ask_insight_retry else R.string.ask_insight_generate),
                        onGenerate,
                        height = 40.dp,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        }
    }
}

private fun bubbleShape(isUser: Boolean) =
    if (isUser) RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp) else RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)

@Composable
private fun Bubble(msg: ChatMsg) {
    val c = AppTheme.colors
    Box(Modifier.fillMaxWidth(), contentAlignment = if (msg.fromUser) Alignment.CenterEnd else Alignment.CenterStart) {
        if (msg.fromUser) {
            Text(
                msg.text,
                style = MaterialTheme.typography.bodyMedium,
                color = c.onAccent,
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(bubbleShape(true))
                    .background(Brush.linearGradient(listOf(c.accent, c.accentDeep)))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
            )
        } else {
            Text(
                richText(msg.text),
                style = MaterialTheme.typography.bodyMedium,
                color = if (msg.isError) c.danger else c.ink,
                modifier = Modifier.widthIn(max = 310.dp).appCard(bubbleShape(false)).padding(horizontal = 14.dp, vertical = 11.dp),
            )
        }
    }
}

@Composable
private fun ThinkingBubble() {
    val c = AppTheme.colors
    Row(
        Modifier.appCard(bubbleShape(false)).padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = c.accent)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.ask_thinking), style = MaterialTheme.typography.bodySmall, color = c.muted)
    }
}

/** Kotak input pil: teks + mic (pengenal suara bawaan, hasil mengisi kotak) + kirim. */
@Composable
private fun InputBar(value: String, onValue: (String) -> Unit, onSend: () -> Unit, sending: Boolean) {
    val c = AppTheme.colors
    val speech = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) {
            r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { heard ->
                onValue(listOf(value.trim(), heard).filter { it.isNotEmpty() }.joinToString(" "))
            }
        }
    }
    val sendEnabled = value.isNotBlank() && !sending
    Row(
        Modifier
            .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 10.dp)
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .appCard(RoundedCornerShape(27.dp))
            .padding(start = 18.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(stringResource(R.string.ask_placeholder), style = MaterialTheme.typography.bodyMedium, color = c.muted)
            BasicTextField(
                value = value,
                onValueChange = onValue,
                maxLines = 4,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.ink),
                cursorBrush = SolidColor(c.accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(
            Modifier.clip(CircleShape).clickable {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                    .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
                runCatching { speech.launch(intent) }
            }.padding(8.dp),
        ) { Sol(R.drawable.sol_mic, tint = c.muted, size = 20.dp, contentDescription = stringResource(R.string.ask_mic)) }
        Box(
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(c.accent.copy(alpha = if (sendEnabled) 1f else 0.4f), c.accentDeep.copy(alpha = if (sendEnabled) 1f else 0.4f))))
                .clickable(enabled = sendEnabled, onClick = onSend),
            contentAlignment = Alignment.Center,
        ) { Sol(R.drawable.sol_send, tint = c.onAccent, size = 18.dp, contentDescription = stringResource(R.string.ask_send)) }
    }
}
