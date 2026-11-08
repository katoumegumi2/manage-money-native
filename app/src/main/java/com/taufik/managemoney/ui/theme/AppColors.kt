package com.taufik.managemoney.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.taufik.managemoney.data.settings.FlowColorMode

/**
 * Token warna Redesign v4. Semua diturunkan dari **satu warna aksen** pilihan user + mode gelap/terang +
 * [FlowColorMode]. Layar memakai [AppTheme.colors]; komponen M3 ikut lewat colorScheme di [ManageMoneyTheme].
 *
 * Aturan (lihat mockup `.superpowers/brainstorm/.../redesign-v12.html`):
 * - Terang: latar #F3F5F8 + gradasi aksen tipis di atas, kartu putih berbayang lembut.
 * - Gelap: latar #090B10 + pendar aksen, kartu kaca (putih 6–7%) bergaris tepi tipis.
 * - Masuk/keluar & kategori: palet tervalidasi buta warna (dataviz validator), lihat [flowColors].
 */
@Immutable
data class AppColors(
    val isDark: Boolean,
    val accent: Color,
    /** Aksen lebih gelap untuk ujung gradasi tombol. */
    val accentDeep: Color,
    /** Teks/ikon di atas aksen (hitam bila aksen sangat terang, mis. kuning). */
    val onAccent: Color,
    /** Isian lembut ber-aksen (latar ikon, chip sumber). */
    val accentSoft: Color,
    /** Aksen yang aman dibaca sebagai teks kecil di atas kartu. */
    val accentText: Color,
    val bg: Color,
    val card: Color,
    /** Garis tepi kartu (gelap: kaca; terang: transparan). */
    val cardBorder: Color,
    /** Isian dalam kartu (ubin detail, field). */
    val field: Color,
    val line: Color,
    val ink: Color,
    val body: Color,
    val muted: Color,
    val income: Color,
    val expense: Color,
    val transfer: Color,
    /** Warna tanda grafik (garis/batang/area) masuk & keluar. */
    val incomeMark: Color,
    val expenseMark: Color,
    val navPill: Color,
    val navActive: Color,
    val navActiveContent: Color,
    val navIcon: Color,
    val fab: Color,
    val fabContent: Color,
    /** Pil terpilih (segmented, chip): terang = tinta gelap, gelap = aksen transparan. */
    val chipOn: Color,
    val chipOnContent: Color,
    val chipOff: Color,
    val chipOffContent: Color,
    val danger: Color,
    val warn: Color,
    /** Palet kategori (urutan tetap, tervalidasi CVD). Slot terakhir = "Lainnya" (abu). */
    val categorical: List<Color>,
)

val DefaultAccent = Color(0xFF2F7BF6)

/** Campur [a] dengan [b]; [t] = porsi [a] (0..1). Setara CSS color-mix(in srgb, a t, b). */
fun mix(a: Color, b: Color, t: Float): Color = lerp(b, a, t.coerceIn(0f, 1f))

/** Teks di atas aksen: gelap bila aksen terang. Ambang sama dengan mockup (luminance > 0.45). */
fun onAccentFor(accent: Color): Color = if (accent.luminance() > 0.45f) Color(0xFF111827) else Color.White

/** Warna masuk/keluar (teks & tanda grafik) per mode. */
data class FlowColors(val income: Color, val expense: Color, val incomeMark: Color, val expenseMark: Color)

fun flowColors(accent: Color, dark: Boolean, mode: FlowColorMode): FlowColors = when (mode) {
    FlowColorMode.FOLLOW_ACCENT -> if (dark) {
        FlowColors(mix(accent, Color.White, 0.65f), Color(0xFFC5CBD5), accent, Color(0xFF5F6878))
    } else {
        FlowColors(mix(accent, Color.Black, 0.72f), Color(0xFF3A4250), accent, Color(0xFFA3ABB8))
    }
    // Pasangan lolos validator (CVD ΔE 10.7 terang / 9.4 gelap).
    FlowColorMode.FIXED -> if (dark) {
        FlowColors(Color(0xFF4CC9A0), Color(0xFFF08562), Color(0xFF199E70), Color(0xFFD95926))
    } else {
        FlowColors(Color(0xFF0B7A68), Color(0xFFC4462A), Color(0xFF0F9A83), Color(0xFFE5603A))
    }
}

private val CategoricalLight = listOf(Color(0xFF2A78D6), Color(0xFFE87BA4), Color(0xFF6250D6), Color(0xFFEDA100), Color(0xFF8A94A6))
private val CategoricalDark = listOf(Color(0xFF3987E5), Color(0xFFD55181), Color(0xFF9085E9), Color(0xFFC98500), Color(0xFF7D8799))

fun appColors(accent: Color, dark: Boolean, flow: FlowColorMode): AppColors {
    val f = flowColors(accent, dark, flow)
    val onAcc = onAccentFor(accent)
    return if (!dark) {
        val ink = Color(0xFF121722)
        AppColors(
            isDark = false,
            accent = accent, accentDeep = mix(accent, Color.Black, 0.78f), onAccent = onAcc,
            accentSoft = mix(accent, Color.White, 0.14f), accentText = mix(accent, Color.Black, 0.78f),
            bg = Color(0xFFF3F5F8), card = Color.White, cardBorder = Color(0x0F121722),
            field = Color(0xFFF5F7FA), line = Color(0xFFEDF0F4),
            ink = ink, body = Color(0xFF475262), muted = Color(0xFF6A7381),
            income = f.income, expense = f.expense, transfer = Color(0xFF5C7390),
            incomeMark = f.incomeMark, expenseMark = f.expenseMark,
            navPill = Color(0xFFE7EAEE), navActive = Color.White, navActiveContent = ink, navIcon = Color(0xFF8C94A1),
            fab = ink, fabContent = Color.White,
            chipOn = ink, chipOnContent = Color.White, chipOff = Color(0xFFEDF0F4), chipOffContent = Color(0xFF4A5262),
            danger = Color(0xFFE5484D), warn = Color(0xFFD98A00),
            categorical = CategoricalLight,
        )
    } else {
        val ink = Color(0xFFEEF1F6)
        AppColors(
            isDark = true,
            accent = accent, accentDeep = mix(accent, Color.Black, 0.78f), onAccent = onAcc,
            accentSoft = accent.copy(alpha = 0.18f), accentText = mix(accent, Color.White, 0.75f),
            bg = Color(0xFF090B10), card = Color(0xFF161A21), cardBorder = Color(0x17FFFFFF),
            field = Color(0x0DFFFFFF), line = Color(0x12FFFFFF),
            ink = ink, body = Color(0xFFB8BFCC), muted = Color(0xFF8C95A5),
            income = f.income, expense = f.expense, transfer = Color(0xFF8FA6C4),
            incomeMark = f.incomeMark, expenseMark = f.expenseMark,
            navPill = Color(0xFF1A1D25), navActive = Color(0x1AFFFFFF), navActiveContent = Color.White, navIcon = Color(0xFF7C8595),
            fab = accent, fabContent = onAcc,
            chipOn = accent.copy(alpha = 0.22f), chipOnContent = Color.White, chipOff = Color(0x0DFFFFFF), chipOffContent = Color(0xFFAEB5C2),
            danger = Color(0xFFF2777A), warn = Color(0xFFF2B640),
            categorical = CategoricalDark,
        )
    }
}

val LocalAppColors = staticCompositionLocalOf { appColors(DefaultAccent, false, FlowColorMode.FOLLOW_ACCENT) }

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
}

/** Daftar aksen siap pakai (sama dengan mockup). */
val AccentPresets: List<Color> = listOf(
    0xFF2F7BF6, 0xFF00A3FF, 0xFF7C5CFF, 0xFFB14CFF, 0xFFFF4D8D, 0xFFFF5A36,
    0xFFFF9F1C, 0xFFF2C200, 0xFF22C55E, 0xFF14B8A6, 0xFF64748B, 0xFF111827,
).map { Color(it) }
