package com.taufik.managemoney.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.taufik.managemoney.ui.theme.Emerald

/**
 * FAB "+" emerald yang menumpuk di tengah bottom nav (redesign v3).
 * - **Tap** → tambah transaksi (Ketik AI).
 * - **Tekan-tahan** → rekam suara.
 * Ring tipis sewarna latar memisahkan tombol dari bar; bayangan emerald memberi kesan terangkat.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddFab(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .shadow(14.dp, CircleShape, spotColor = Emerald, ambientColor = Emerald)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.background) // ring sewarna latar
            .padding(4.dp)
            .clip(CircleShape)
            .background(Emerald)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(30.dp),
        )
    }
}
