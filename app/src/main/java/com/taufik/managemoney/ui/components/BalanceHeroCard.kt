package com.taufik.managemoney.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taufik.managemoney.ui.theme.BalanceCardBg
import com.taufik.managemoney.ui.theme.MonoFamily
import com.taufik.managemoney.ui.theme.MutedOnDark

/**
 * Kartu "total" gelap (redesign v3 — Total saldo / Arus kas bersih).
 * Permukaan tetap gelap (#0B0F19) di light & dark theme, sesuai handoff.
 *
 * @param label  caption kecil di atas angka (mis. "Total saldo · 6 akun").
 * @param amount nominal sudah terformat (mis. "Rp 2.111.000" atau "+Rp 2.101.000").
 * @param amountColor warna angka utama (putih untuk saldo, hijau/merah untuk net).
 * @param content baris statistik (Masuk/Keluar) + info tambahan di bawah angka.
 */
@Composable
fun BalanceHeroCard(
    label: String,
    amount: String,
    modifier: Modifier = Modifier,
    amountColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(BalanceCardBg)
            // Border halus agar kartu gelap tetap tegas di atas background gelap (dark theme).
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(22.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = MutedOnDark,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = amount,
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 27.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.7).sp,
            color = amountColor,
            modifier = Modifier.padding(top = 3.dp),
        )
        Spacer(Modifier.height(13.dp))
        content()
    }
}

/** Satu kolom statistik kecil pada kartu gelap (label di atas, nominal di bawah). */
@Composable
fun BalanceHeroStat(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = MutedOnDark,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = value,
            fontFamily = MonoFamily,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
