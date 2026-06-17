package com.taufik.managemoney.ui.share

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.taufik.managemoney.R
import com.taufik.managemoney.ui.confirm.ConfirmScreen
import com.taufik.managemoney.ui.navigation.ARG_CONFIRM_PAYLOAD
import com.taufik.managemoney.ui.navigation.CONFIRM_ROUTE
import com.taufik.managemoney.ui.navigation.confirmRoute
import com.taufik.managemoney.ui.theme.ManageMoneyTheme
import dagger.hilt.android.AndroidEntryPoint

private const val SHARE_LOADING_ROUTE = "share_loading"

/** Penerima "Bagikan" (share sheet): teks/gambar bukti bayar → AI → Konfirmasi → simpan. */
@AndroidEntryPoint
class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = extractContent()
        if (content == null) {
            Toast.makeText(this, getString(R.string.share_unsupported), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            ManageMoneyTheme(darkTheme = isSystemInDarkTheme()) {
                ShareFlow(content = content, onFinish = { finish() })
            }
        }
    }

    private fun extractContent(): SharedContent? {
        val type = intent?.type ?: return null
        return when (intent?.action) {
            Intent.ACTION_SEND -> when {
                type == "text/plain" ->
                    intent.getStringExtra(Intent.EXTRA_TEXT)?.takeIf { it.isNotBlank() }?.let { SharedContent.Text(it) }
                type.startsWith("image/") ->
                    IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
                        ?.let { SharedContent.Images(listOf(it)) }
                else -> null
            }
            Intent.ACTION_SEND_MULTIPLE -> if (type.startsWith("image/")) {
                IntentCompat.getParcelableArrayListExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
                    ?.takeIf { it.isNotEmpty() }?.let { SharedContent.Images(it) }
            } else null
            else -> null
        }
    }
}

@Composable
private fun ShareFlow(content: SharedContent, onFinish: () -> Unit) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    NavHost(navController = navController, startDestination = SHARE_LOADING_ROUTE) {
        composable(SHARE_LOADING_ROUTE) {
            ShareEntryScreen(
                content = content,
                onParsed = { payload ->
                    navController.navigate(confirmRoute(payload)) {
                        popUpTo(SHARE_LOADING_ROUTE) { inclusive = true }
                    }
                },
                onClose = onFinish,
            )
        }
        composable(
            route = CONFIRM_ROUTE,
            arguments = listOf(navArgument(ARG_CONFIRM_PAYLOAD) { type = NavType.StringType }),
        ) {
            ConfirmScreen(
                onBack = onFinish,
                onSaved = { count ->
                    Toast.makeText(
                        context,
                        context.resources.getQuantityString(R.plurals.saved_count, count, count),
                        Toast.LENGTH_SHORT,
                    ).show()
                    onFinish()
                },
            )
        }
    }
}

@Composable
private fun ShareEntryScreen(
    content: SharedContent,
    onParsed: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: ShareReceiverViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.process(content) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (val s = state) {
                is ShareState.Loading -> {
                    CircularProgressIndicator()
                    Text(
                        stringResource(R.string.share_processing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
                is ShareState.Ready -> LaunchedEffect(s) { onParsed(s.payload) }
                is ShareState.Empty -> Message(stringResource(R.string.share_empty), onClose)
                is ShareState.Error -> Message(s.message, onClose)
            }
        }
    }
}

@Composable
private fun Message(text: String, onClose: () -> Unit) {
    Text(text, style = MaterialTheme.typography.bodyLarge)
    TextButton(onClick = onClose, modifier = Modifier.padding(top = 12.dp)) {
        Text(stringResource(R.string.action_close))
    }
}
