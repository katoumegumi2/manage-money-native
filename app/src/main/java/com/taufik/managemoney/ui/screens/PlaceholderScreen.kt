package com.taufik.managemoney.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taufik.managemoney.ui.components.AmountTextLarge
import com.taufik.managemoney.ui.theme.ManageMoneyTheme

/**
 * Layar sementara Phase 0 — memverifikasi tema, font, dan render AmountText.
 * Diganti layar asli pada fase berikut.
 */
@Composable
fun PlaceholderScreen(title: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Card {
            AmountTextLarge(
                amount = 1_234_567,
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = "Segera hadir",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderPreviewLight() {
    ManageMoneyTheme(darkTheme = false) { PlaceholderScreen("Beranda") }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderPreviewDark() {
    ManageMoneyTheme(darkTheme = true) { PlaceholderScreen("Beranda") }
}
