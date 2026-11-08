package com.taufik.managemoney.ui.confirm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
import com.taufik.managemoney.core.formatGrouped
import com.taufik.managemoney.domain.model.Account
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.domain.model.DraftIssue
import com.taufik.managemoney.ui.components.AccountAvatar
import com.taufik.managemoney.ui.components.AccountPicker
import com.taufik.managemoney.ui.components.CatatanField
import com.taufik.managemoney.ui.components.CircleIconButton
import com.taufik.managemoney.ui.components.GhostPillButton
import com.taufik.managemoney.ui.components.KategoriPicker
import com.taufik.managemoney.ui.components.NominalField
import com.taufik.managemoney.ui.components.PrimaryPillButton
import com.taufik.managemoney.ui.components.SegmentedPills
import com.taufik.managemoney.ui.components.SmallPill
import com.taufik.managemoney.ui.components.Sol
import com.taufik.managemoney.ui.components.TanggalField
import com.taufik.managemoney.ui.components.TransferAccounts
import com.taufik.managemoney.ui.components.appCard
import com.taufik.managemoney.ui.components.screenBackground
import com.taufik.managemoney.ui.model.EditableTx
import com.taufik.managemoney.ui.theme.AppTheme
import com.taufik.managemoney.ui.theme.MoneyStyle
import com.taufik.managemoney.ui.util.accountVisual
import com.taufik.managemoney.ui.util.categoryVisual
import com.taufik.managemoney.ui.util.kategoriLabel
import com.taufik.managemoney.ui.util.txTypeLabel
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("id-ID"))

/**
 * Konfirmasi v4 (gaya struk): tiap transaksi hasil AI/manual = kartu struk dengan nominal besar,
 * pil tipe, sobekan putus-putus, dan ubin detail. Ketuk ubin → editor lengkap terbuka di bawahnya.
 */
@Composable
fun ConfirmScreen(
    onBack: () -> Unit,
    onSaved: (Int) -> Unit,
    viewModel: ConfirmViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    LaunchedEffect(state.savedCount) { state.savedCount?.let { onSaved(it) } }

    Box(
        Modifier
            .fillMaxSize()
            .screenBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                CircleIconButton(R.drawable.sol_arrow_left, onBack, stringResource(R.string.action_back))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.confirm_title), style = MaterialTheme.typography.titleMedium, color = c.ink)
                    if (state.items.isNotEmpty()) {
                        Text(
                            pluralStringResource(R.plurals.confirm_count, state.items.size, state.items.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = c.muted,
                        )
                    }
                }
                Spacer(Modifier.size(42.dp))
            }
            when {
                state.loading -> Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) { CircularProgressIndicator(color = c.accent) }
                state.items.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                    Text(stringResource(R.string.confirm_empty), style = MaterialTheme.typography.bodyMedium, color = c.muted)
                }
                else -> LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    state.errorMessage?.let { msg ->
                        item {
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.danger.copy(alpha = 0.12f)).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Sol(R.drawable.sol_warning, tint = c.danger, size = 18.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(msg, style = MaterialTheme.typography.bodySmall, color = c.danger)
                            }
                        }
                    }
                    itemsIndexed(state.items) { index, item ->
                        ReceiptCard(
                            item = item,
                            accounts = state.accounts,
                            showErrors = state.showErrors,
                            canRemove = state.items.size > 1,
                            onTipe = { viewModel.setTipe(index, it) },
                            onNominal = { viewModel.setNominal(index, it) },
                            onKategori = { viewModel.setKategori(index, it) },
                            onAkunDari = { viewModel.setAkunDari(index, it) },
                            onAkunKe = { viewModel.setAkunKe(index, it) },
                            onTanggal = { viewModel.setTanggal(index, it) },
                            onCatatan = { viewModel.setCatatan(index, it) },
                            onRemove = { viewModel.removeItem(index) },
                        )
                    }
                }
            }
        }
        if (!state.loading && state.items.isNotEmpty()) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(c.bg.copy(alpha = 0f), c.bg.copy(alpha = 0.95f), c.bg)))
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GhostPillButton(stringResource(R.string.action_cancel), onBack, modifier = Modifier.width(104.dp))
                    PrimaryPillButton(
                        if (state.saving) stringResource(R.string.confirm_saving) else stringResource(R.string.confirm_save_all, state.items.size),
                        onClick = viewModel::save,
                        enabled = state.canSave && !state.saving,
                        icon = R.drawable.sol_check,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptCard(
    item: EditableTx,
    accounts: List<Account>,
    showErrors: Boolean,
    canRemove: Boolean,
    onTipe: (TxType) -> Unit,
    onNominal: (Long) -> Unit,
    onKategori: (String) -> Unit,
    onAkunDari: (String) -> Unit,
    onAkunKe: (String) -> Unit,
    onTanggal: (java.time.LocalDate) -> Unit,
    onCatatan: (String) -> Unit,
    onRemove: () -> Unit,
) {
    val c = AppTheme.colors
    val issues = item.issues
    val hasError = showErrors && issues.isNotEmpty()
    var editing by rememberSaveable { mutableStateOf(false) }
    val open = editing || hasError
    val shape = RoundedCornerShape(26.dp)
    val byId = accounts.associateBy { it.id }
    val amountColor = when (item.tipe) {
        TxType.PENGELUARAN -> c.expense
        TxType.PEMASUKAN -> c.income
        TxType.TRANSFER -> c.transfer
    }
    Column(
        Modifier
            .fillMaxWidth()
            .appCard(shape)
            .then(if (item.perluDicek) Modifier.border(1.5.dp, c.warn, shape) else Modifier)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (item.perluDicek) {
                SmallPill(stringResource(R.string.confirm_need_check_short), icon = R.drawable.sol_warning, container = c.warn.copy(alpha = 0.14f), content = c.warn)
            } else {
                SmallPill(stringResource(R.string.confirm_check_then_save), icon = R.drawable.sol_ai, container = c.accentSoft, content = if (c.isDark) Color.White else c.accentText)
            }
            Spacer(Modifier.weight(1f))
            if (canRemove) {
                Box(Modifier.clip(CircleShape).clickable(onClick = onRemove).padding(6.dp)) {
                    Sol(R.drawable.sol_trash, tint = c.muted, size = 18.dp, contentDescription = stringResource(R.string.action_delete))
                }
            }
        }
        if (item.catatan.isNotBlank()) {
            Text(
                item.catatan,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = c.ink,
                modifier = Modifier.padding(top = 14.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            Modifier.padding(top = 12.dp, bottom = 14.dp).clickable { editing = !editing },
            verticalAlignment = Alignment.Bottom,
        ) {
            val sign = when (item.tipe) { TxType.PENGELUARAN -> "−"; TxType.PEMASUKAN -> "+"; TxType.TRANSFER -> "" }
            if (sign.isNotEmpty()) Text(sign, style = MoneyStyle.copy(fontSize = 26.sp), color = amountColor, modifier = Modifier.padding(end = 6.dp, bottom = 3.dp))
            Text("Rp", style = MaterialTheme.typography.titleMedium, color = c.muted, modifier = Modifier.padding(end = 6.dp, bottom = 5.dp))
            Text(formatGrouped(item.nominal), style = MoneyStyle.copy(fontSize = 40.sp, letterSpacing = (-1.5).sp), color = c.ink, maxLines = 1)
        }
        SegmentedPills(
            options = listOf(TxType.PENGELUARAN, TxType.PEMASUKAN, TxType.TRANSFER).map { it to txTypeLabel(it) },
            selected = item.tipe,
            onSelect = onTipe,
        )
        DashedCut()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.tipe != TxType.TRANSFER) {
                    DetailTile(stringResource(R.string.confirm_tile_category), Modifier.weight(1f), onClick = { editing = true }) {
                        Sol(categoryVisual(item.tipe, item.kategori).icon, tint = c.accent, size = 17.dp)
                        Text(kategoriLabel(item.kategori), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = c.ink, maxLines = 1)
                    }
                }
                val accId = if (item.tipe == TxType.PEMASUKAN) item.akunKeId else item.akunDariId
                val accLabel = stringResource(
                    when (item.tipe) {
                        TxType.PEMASUKAN -> R.string.confirm_tile_to
                        else -> R.string.confirm_tile_from
                    },
                )
                AccountTile(accLabel, accId?.let { byId[it] }, Modifier.weight(1f)) { editing = true }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.tipe == TxType.TRANSFER) {
                    AccountTile(stringResource(R.string.confirm_tile_to), item.akunKeId?.let { byId[it] }, Modifier.weight(1f)) { editing = true }
                }
                DetailTile(stringResource(R.string.confirm_tile_date), Modifier.weight(1f), onClick = { editing = true }) {
                    Sol(R.drawable.sol_calendar, tint = c.accent, size = 17.dp)
                    Text(item.tanggal.format(dateFmt), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = c.ink, maxLines = 1)
                }
            }
        }
        if (hasError) {
            Text(issueHint(issues), style = MaterialTheme.typography.labelMedium, color = c.danger, modifier = Modifier.padding(top = 10.dp))
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 10.dp).clip(CircleShape).clickable { editing = !open }.padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Sol(if (open) R.drawable.sol_chevron_down else R.drawable.sol_pen, tint = c.accentText, size = 16.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(if (open) R.string.confirm_hide_editor else R.string.confirm_edit_detail),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = c.accentText,
            )
        }
        AnimatedVisibility(open) {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NominalField(value = item.nominal, onValue = onNominal, isError = showErrors && DraftIssue.NOMINAL_TIDAK_VALID in issues)
                if (item.tipe != TxType.TRANSFER) KategoriPicker(tipe = item.tipe, selected = item.kategori, onSelect = onKategori)
                when (item.tipe) {
                    TxType.PENGELUARAN -> AccountPicker(
                        label = stringResource(R.string.field_from_short), accounts = accounts, selectedId = item.akunDariId,
                        onSelect = onAkunDari, isError = showErrors && DraftIssue.AKUN_DARI_WAJIB in issues,
                    )
                    TxType.PEMASUKAN -> AccountPicker(
                        label = stringResource(R.string.field_to_short), accounts = accounts, selectedId = item.akunKeId,
                        onSelect = onAkunKe, isError = showErrors && DraftIssue.AKUN_KE_WAJIB in issues,
                    )
                    TxType.TRANSFER -> TransferAccounts(
                        accounts = accounts, fromId = item.akunDariId, toId = item.akunKeId, onFrom = onAkunDari, onTo = onAkunKe,
                        fromError = showErrors && DraftIssue.AKUN_DARI_WAJIB in issues,
                        toError = showErrors && (DraftIssue.AKUN_KE_WAJIB in issues || DraftIssue.TRANSFER_AKUN_SAMA in issues),
                    )
                }
                TanggalField(value = item.tanggal, onValue = onTanggal)
                CatatanField(value = item.catatan, onValue = onCatatan)
            }
        }
    }
}

/** Garis putus-putus selebar kartu (sobekan struk). */
@Composable
private fun DashedCut() {
    val line = AppTheme.colors.line
    Canvas(Modifier.fillMaxWidth().padding(vertical = 16.dp).height(2.dp)) {
        drawLine(
            line, Offset(0f, size.height / 2), Offset(size.width, size.height / 2),
            strokeWidth = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
        )
    }
}

@Composable
private fun DetailTile(label: String, modifier: Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    val c = AppTheme.colors
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(c.field)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = c.muted)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { content() }
    }
}

@Composable
private fun AccountTile(label: String, account: Account?, modifier: Modifier, onClick: () -> Unit) {
    val c = AppTheme.colors
    DetailTile(label, modifier, onClick) {
        if (account != null) {
            val v = accountVisual(account.kode, account.jenis)
            AccountAvatar(kode = account.kode, abbr = v.abbr, color = v.color, size = 20, circle = true)
            Text(account.nama, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = c.ink, maxLines = 1)
        } else {
            Text(stringResource(R.string.confirm_pick_account), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = c.danger, textAlign = TextAlign.Start)
        }
    }
}

@Composable
private fun issueHint(issues: Set<DraftIssue>): String = when {
    DraftIssue.NOMINAL_TIDAK_VALID in issues -> stringResource(R.string.issue_nominal)
    DraftIssue.TRANSFER_AKUN_SAMA in issues -> stringResource(R.string.issue_transfer_same)
    DraftIssue.AKUN_DARI_WAJIB in issues || DraftIssue.AKUN_KE_WAJIB in issues -> stringResource(R.string.issue_account_required)
    else -> ""
}
