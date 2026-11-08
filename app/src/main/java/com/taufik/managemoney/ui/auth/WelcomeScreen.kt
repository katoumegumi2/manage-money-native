package com.taufik.managemoney.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.ui.components.PrimaryPillButton
import com.taufik.managemoney.ui.components.screenBackground
import androidx.compose.ui.draw.clip

/** Layar sambutan onboarding: login Google (dianjurkan) atau pakai tanpa backup. */
@Composable
fun WelcomeScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    if (message != null) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.messageShown()
    }

    val c = com.taufik.managemoney.ui.theme.AppTheme.colors
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().screenBackground()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(R.drawable.logo_app),
                contentDescription = null,
                modifier = Modifier.size(96.dp).clip(RoundedCornerShape(26.dp)),
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))

            if (viewModel.isLoginConfigured) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp, color = c.accent)
                } else {
                    PrimaryPillButton(
                        stringResource(R.string.welcome_sign_in), { viewModel.signIn(context) },
                        icon = R.drawable.sol_cloud_check, height = 52.dp, modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            TextButton(onClick = { viewModel.skipBackup() }, enabled = !busy) {
                Text(stringResource(R.string.welcome_skip))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.welcome_skip_note),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
