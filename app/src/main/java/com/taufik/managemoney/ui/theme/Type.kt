package com.taufik.managemoney.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.taufik.managemoney.R

// Redesign v3: Plus Jakarta Sans = satu-satunya family untuk seluruh UI.
// (lihat docs/private/design_handoff_catatan_keuangan)
val PlusJakarta = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

// Alias lama tetap menunjuk PJS → call-site (display/body/angka) tak perlu diubah massal.
val SpaceGrotesk = PlusJakarta
val Inter = PlusJakarta
val MonoFamily = PlusJakarta

// Fitur angka tabular (rata lebar digit) — dipakai di komponen nominal.
const val NumberFontFeature = "tnum"

val AppTypography = Typography(
    // Hero nominal detail (36/800, tracking rapat)
    displayLarge = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = (-0.9).sp, fontFeatureSettings = NumberFontFeature),
    // Total saldo (27/800)
    displayMedium = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp, lineHeight = 32.sp, letterSpacing = (-0.7).sp, fontFeatureSettings = NumberFontFeature),
    // Judul layar (25/800)
    headlineMedium = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.ExtraBold, fontSize = 25.sp, lineHeight = 30.sp, letterSpacing = (-0.5).sp, fontFeatureSettings = NumberFontFeature),
    headlineSmall = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.3).sp, fontFeatureSettings = NumberFontFeature),
    titleLarge = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.3).sp, fontFeatureSettings = NumberFontFeature),
    // Header section (15/700)
    titleMedium = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp, fontFeatureSettings = NumberFontFeature),
    titleSmall = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp, fontFeatureSettings = NumberFontFeature),
    bodyLarge = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, fontFeatureSettings = NumberFontFeature),
    // Judul baris transaksi/akun (~14.5/600 — caller set weight)
    bodyMedium = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, fontFeatureSettings = NumberFontFeature),
    // Nominal di list / link (14/600)
    labelLarge = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp, fontFeatureSettings = NumberFontFeature),
    bodySmall = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, fontFeatureSettings = NumberFontFeature),
    // Meta / caption (12/500)
    labelMedium = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, fontFeatureSettings = NumberFontFeature),
    // Label nav / caption uppercase (10–11/600)
    labelSmall = TextStyle(fontFamily = PlusJakarta, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp, fontFeatureSettings = NumberFontFeature),
)
