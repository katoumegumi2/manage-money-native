package com.taufik.managemoney.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taufik.managemoney.ui.theme.SpaceGrotesk

/**
 * Badge brand akun (fallback bila logo asli belum ada): squircle dengan gradien diagonal
 * dari warna brand + kilau halus di tepi atas, agar terasa seperti ikon, bukan kotak datar.
 */
@Composable
fun AccountBadge(
    abbr: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Int = 38,
) {
    val corner = (size * 0.30f).dp
    val shape = RoundedCornerShape(corner)
    // Gradien: lebih terang di kiri-atas → warna brand → sedikit gelap di kanan-bawah.
    val light = lerp(color, Color.White, 0.22f)
    val dark = lerp(color, Color.Black, 0.30f)
    Box(
        modifier = modifier
            .size(size.dp)
            .shadow(elevation = (size * 0.06f).dp, shape = shape, clip = false)
            .clip(shape)
            .background(Brush.linearGradient(listOf(light, color, dark)))
            // Kilau tipis di tepi (highlight) untuk kesan timbul.
            .border(width = (size * 0.025f).dp, color = Color.White.copy(alpha = 0.28f), shape = shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = abbr,
            color = Color.White,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.33f).sp,
            textAlign = TextAlign.Center,
        )
    }
}
