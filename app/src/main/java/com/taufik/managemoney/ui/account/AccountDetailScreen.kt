package com.taufik.managemoney.ui.account

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import com.taufik.managemoney.ui.components.LocalBottomBarSpace
import com.taufik.managemoney.ui.components.AccountAvatar
import com.taufik.managemoney.ui.components.SubScreenHeader
import com.taufik.managemoney.ui.components.appCard
import com.taufik.managemoney.ui.components.screenBackground

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.core.formatRupiah
import com.taufik.managemoney.ui.components.TxRow
import com.taufik.managemoney.ui.theme.MonoFamily
import com.taufik.managemoney.ui.theme.SpaceGrotesk
import com.taufik.managemoney.ui.util.accountVisual
import com.taufik.managemoney.ui.util.jenisLabel

@Composable
fun AccountDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (!state.found && !state.loading) {
        NotFound(onBack, modifier)
        return
    }

    val c = com.taufik.managemoney.ui.theme.AppTheme.colors
    Column(modifier.fillMaxSize().screenBackground().statusBarsPadding()) {
    SubScreenHeader(state.nama, onBack)
    LazyColumn(
        Modifier.weight(1f),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, bottom = LocalBottomBarSpace.current + 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            val shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp)
            val inner = androidx.compose.foundation.shape.RoundedCornerShape(25.dp)
            Column(Modifier.fillMaxWidth().appCard(shape).padding(6.dp)) {
                Column(
                    Modifier.fillMaxWidth().clip(inner)
                        .background(
                            if (c.isDark) androidx.compose.ui.graphics.Brush.linearGradient(listOf(com.taufik.managemoney.ui.theme.mix(c.accent, Color(0xFF11141B), 0.30f), Color(0xFF11141B)))
                            else androidx.compose.ui.graphics.Brush.linearGradient(listOf(com.taufik.managemoney.ui.theme.mix(c.accent, Color.White, 0.16f), com.taufik.managemoney.ui.theme.mix(c.accent, Color.White, 0.40f))),
                        )
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.jenis != null) {
                            val v = accountVisual(state.kode, state.jenis!!)
                            AccountAvatar(kode = state.kode, abbr = v.abbr, color = v.color, size = 34, circle = true)
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(state.jenis?.let { jenisLabel(it) } ?: "", style = MaterialTheme.typography.labelLarge, color = c.ink)
                    }
                    Text(
                        formatRupiah(state.saldo),
                        style = com.taufik.managemoney.ui.theme.MoneyStyle.copy(fontSize = 32.sp),
                        color = if (state.saldo < 0) c.expense else c.ink,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
                Text(
                    stringResource(R.string.account_initial_balance, formatRupiah(state.saldoAwal)),
                    style = MaterialTheme.typography.labelMedium,
                    color = c.body,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
        }
        item { com.taufik.managemoney.ui.components.SectionHeader(stringResource(R.string.account_mutations)) }
        item {
            Column(Modifier.fillMaxWidth().appCard().padding(horizontal = 14.dp, vertical = 2.dp)) {
                if (state.mutasi.isEmpty() && !state.loading) {
                    Text(stringResource(R.string.account_no_mutations), style = MaterialTheme.typography.bodyMedium, color = c.muted, modifier = Modifier.padding(vertical = 24.dp))
                } else {
                    state.mutasi.forEach { row -> TxRow(row, modifier = Modifier.fillMaxWidth()) }
                }
            }
        }
    }
    }
}

@Composable
private fun NotFound(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.account_not_found),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.nav_home),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.clickable(onClick = onBack),
        )
    }
}
