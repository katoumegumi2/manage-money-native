package com.taufik.managemoney.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taufik.managemoney.ui.theme.SpaceGrotesk

/** Badge persegi-bulat berwarna brand akun dengan singkatan (mis. "BNI", "GP"). */
@Composable
fun AccountBadge(
    abbr: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Int = 38,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size * 0.29f).dp))
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = abbr,
            color = Color.White,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.34f).sp,
            textAlign = TextAlign.Center,
        )
    }
}
