package com.taufik.managemoney.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taufik.managemoney.ui.theme.SpaceGrotesk
import java.util.Locale

private val localeId = Locale.forLanguageTag("id-ID")

/**
 * Avatar akun **logo-ready**: kalau ada drawable bernama `logo_<kode>` (mis. `logo_bca.png`),
 * tampilkan logo asli; kalau belum ada, fallback ke [AccountBadge] (kotak warna brand + inisial).
 *
 * Cara menambah logo: taruh file di `app/src/main/res/drawable/` dengan nama `logo_<kode>.png`
 * (atau .webp/.xml), kode huruf kecil sesuai akun — mis. `logo_ovo.png`, `logo_dana.png`.
 * Resolusi pakai nama (getIdentifier) supaya tak perlu ubah kode saat menambah file.
 */
@Composable
fun AccountAvatar(
    kode: String,
    abbr: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Int = 38,
    /** v4: logo dalam lingkaran putih (kartu akun). */
    circle: Boolean = false,
) {
    val context = LocalContext.current
    val logoRes = remember(kode) {
        context.resources.getIdentifier(
            "logo_${kode.lowercase(localeId)}",
            "drawable",
            context.packageName,
        )
    }
    when {
        logoRes != 0 -> {
            val shape = if (circle) androidx.compose.foundation.shape.CircleShape else RoundedCornerShape((size * 0.29f).dp)
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = modifier
                    .size(size.dp)
                    .clip(shape)
                    .background(Color.White)
                    .border(1.dp, Color(0x14000000), shape)
                    .padding((size * if (circle) 0.14f else 0.12f).dp),
            )
        }
        // Tunai tanpa logo file → ilustrasi uang tunai (uang kertas + "Rp").
        kode.lowercase(localeId) == "tunai" -> RupiahAvatar(size = size, modifier = modifier)
        else -> AccountBadge(abbr = abbr, color = color, size = size, modifier = modifier)
    }
}

/** Ilustrasi uang tunai: petak hijau (gradien) berisi "uang kertas" putih dengan teks Rp. */
@Composable
private fun RupiahAvatar(size: Int, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape((size * 0.30f).dp)
    val green = Color(0xFF2F7D5B)
    val light = lerp(green, Color.White, 0.18f)
    val dark = lerp(green, Color.Black, 0.28f)
    Box(
        modifier = modifier
            .size(size.dp)
            .shadow(elevation = (size * 0.06f).dp, shape = shape, clip = false)
            .clip(shape)
            .background(Brush.linearGradient(listOf(light, green, dark)))
            .border(width = (size * 0.025f).dp, color = Color.White.copy(alpha = 0.28f), shape = shape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.66f)
                .fillMaxHeight(0.46f)
                .clip(RoundedCornerShape((size * 0.10f).dp))
                .background(Color.White.copy(alpha = 0.94f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Rp",
                color = green,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.26f).sp,
            )
        }
    }
}
