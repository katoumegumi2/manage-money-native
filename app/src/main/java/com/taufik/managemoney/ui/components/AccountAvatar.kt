package com.taufik.managemoney.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
) {
    val context = LocalContext.current
    val logoRes = remember(kode) {
        context.resources.getIdentifier(
            "logo_${kode.lowercase(localeId)}",
            "drawable",
            context.packageName,
        )
    }
    if (logoRes != 0) {
        val corner = (size * 0.29f).dp
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier
                .size(size.dp)
                .clip(RoundedCornerShape(corner))
                .background(Color.White)
                .border(1.dp, Color(0x14000000), RoundedCornerShape(corner))
                .padding((size * 0.12f).dp),
        )
    } else {
        AccountBadge(abbr = abbr, color = color, size = size, modifier = modifier)
    }
}
