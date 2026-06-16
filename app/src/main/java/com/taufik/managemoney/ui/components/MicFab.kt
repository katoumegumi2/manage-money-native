package com.taufik.managemoney.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Tombol mic bulat emas dengan ring border warna latar (paper/ink) — meniru FAB mockup
 * yang menumpuk di tepi atas bottom nav. Ring memberi kesan "tercoak" dari bar.
 */
@Composable
fun MicFab(
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .shadow(10.dp, CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.background) // ring (border) sewarna latar
            .padding(5.dp)
            .clip(CircleShape)
            .background(Color(0xFFD99A2B)) // gold
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = contentDescription,
            tint = Color(0xFF1B1306),
            modifier = Modifier.size(28.dp),
        )
    }
}
