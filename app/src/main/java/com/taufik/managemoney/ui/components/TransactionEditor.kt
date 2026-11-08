package com.taufik.managemoney.ui.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taufik.managemoney.R
import com.taufik.managemoney.core.formatGrouped
import com.taufik.managemoney.domain.model.Account
import com.taufik.managemoney.domain.model.Categories
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.theme.AppTheme
import com.taufik.managemoney.ui.theme.MoneyStyle
import com.taufik.managemoney.ui.util.accountVisual
import com.taufik.managemoney.ui.util.categoryVisual
import com.taufik.managemoney.ui.util.kategoriLabel
import com.taufik.managemoney.ui.util.txTypeLabel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// =====================================================================================
// Editor transaksi v4 — dipakai Konfirmasi, Edit transaksi, dan Tambah manual.
// Kategori = chip berikon Solar, dompet = chip berlogo, nominal = angka mono besar.
// =====================================================================================

private val localeId = Locale.forLanguageTag("id-ID")
private val tanggalFormatter = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", localeId)

/** Label kecil di atas field editor. */
@Composable
private fun FieldLabel(text: String, error: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = if (error) AppTheme.colors.danger else AppTheme.colors.muted,
        modifier = Modifier.padding(start = 2.dp, bottom = 6.dp),
    )
}

@Composable
fun TipeSegmented(
    selected: TxType,
    onSelect: (TxType) -> Unit,
    modifier: Modifier = Modifier,
) {
    SegmentedPills(
        options = TxType.entries.map { it to txTypeLabel(it) },
        selected = selected,
        onSelect = onSelect,
        fill = true,
        modifier = modifier.fillMaxWidth(),
    )
}

/** Input nominal rupiah: "Rp" + angka mono besar bergrup titik. Menyimpan Long. */
@Composable
fun NominalField(
    value: Long,
    onValue: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val c = AppTheme.colors
    Column(modifier.fillMaxWidth()) {
        FieldLabel(stringResource(R.string.field_nominal), isError)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(c.field)
                .border(1.5.dp, if (isError) c.danger else c.line, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Rp", style = MaterialTheme.typography.titleMedium, color = c.muted)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (value == 0L) Text("0", style = MoneyStyle.copy(fontSize = 28.sp), color = c.muted.copy(alpha = 0.5f))
                BasicTextField(
                    value = if (value == 0L) "" else formatGrouped(value),
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(15)
                        onValue(digits.toLongOrNull() ?: 0L)
                    },
                    singleLine = true,
                    textStyle = MoneyStyle.copy(fontSize = 28.sp, color = c.ink),
                    cursorBrush = SolidColor(c.accent),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Kategori sesuai tipe sebagai chip berikon (terpilih = gaya aktif). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KategoriPicker(
    tipe: TxType,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    Column(modifier.fillMaxWidth()) {
        FieldLabel(stringResource(R.string.confirm_tile_category))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Categories.forType(tipe).forEach { k ->
                val on = k == selected
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(if (on) c.chipOn else c.field)
                        .then(if (on && c.isDark) Modifier.border(1.5.dp, c.accent, CircleShape) else Modifier)
                        .clickable { onSelect(k) }
                        .padding(start = 10.dp, end = 13.dp, top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Sol(categoryVisual(tipe, k).icon, tint = if (on) c.chipOnContent else c.accentText, size = 16.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(kategoriLabel(k), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = if (on) c.chipOnContent else c.chipOffContent)
                }
            }
        }
    }
}

/** Pilih dompet (WAJIB): chip berlogo yang bisa digeser. [label] = "Dari dompet" / "Ke dompet". */
@Composable
fun AccountPicker(
    label: String,
    accounts: List<Account>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val c = AppTheme.colors
    Column(modifier.fillMaxWidth()) {
        FieldLabel(if (isError) "$label · ${stringResource(R.string.confirm_pick_account).lowercase(localeId)}" else label, isError)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            accounts.forEach { acc ->
                val on = acc.id == selectedId
                val v = accountVisual(acc.kode, acc.jenis)
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(if (on) c.chipOn else c.field)
                        .border(
                            if (on && c.isDark) 1.5.dp else 1.dp,
                            when {
                                on && c.isDark -> c.accent
                                isError -> c.danger.copy(alpha = 0.6f)
                                else -> Color.Transparent
                            },
                            CircleShape,
                        )
                        .clickable { onSelect(acc.id) }
                        .padding(start = 5.dp, end = 14.dp, top = 5.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AccountAvatar(kode = acc.kode, abbr = v.abbr, color = v.color, size = 26, circle = true)
                    Spacer(Modifier.width(7.dp))
                    Text(acc.nama, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = if (on) c.chipOnContent else c.chipOffContent, maxLines = 1)
                }
            }
        }
    }
}

/** Transfer: pilih dompet asal & tujuan (harus beda). */
@Composable
fun TransferAccounts(
    accounts: List<Account>,
    fromId: String?,
    toId: String?,
    onFrom: (String) -> Unit,
    onTo: (String) -> Unit,
    fromError: Boolean,
    toError: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AccountPicker(stringResource(R.string.field_account_from), accounts, fromId, onFrom, isError = fromError)
        AccountPicker(stringResource(R.string.field_account_to), accounts, toId, onTo, isError = toError)
    }
}

/** Tanggal: pilihan cepat Hari ini / Kemarin + tombol kalender (date picker). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TanggalField(
    value: LocalDate,
    onValue: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val zone = ZoneId.of("Asia/Jakarta")
    val today = remember { LocalDate.now(zone) }
    var showDialog by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth()) {
        FieldLabel(stringResource(R.string.confirm_tile_date))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            SmallPill(stringResource(R.string.date_today), on = value == today, onClick = { onValue(today) })
            SmallPill(stringResource(R.string.date_yesterday), on = value == today.minusDays(1), onClick = { onValue(today.minusDays(1)) })
            Row(
                Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (value != today && value != today.minusDays(1)) c.chipOn else c.field)
                    .clickable { showDialog = true }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val custom = value != today && value != today.minusDays(1)
                Sol(R.drawable.sol_calendar, tint = if (custom) c.chipOnContent else c.accentText, size = 15.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    value.format(tanggalFormatter),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (custom) c.chipOnContent else c.chipOffContent,
                    maxLines = 1,
                )
            }
        }
    }
    if (showDialog) {
        val state = rememberDatePickerState(initialSelectedDateMillis = value.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis -> onValue(Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()) }
                    showDialog = false
                }) { Text(stringResource(R.string.date_pick)) }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.action_cancel)) } },
        ) { DatePicker(state = state) }
    }
}

/** Catatan opsional. */
@Composable
fun CatatanField(
    value: String,
    onValue: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    Column(modifier.fillMaxWidth()) {
        FieldLabel(stringResource(R.string.field_note))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(c.field)
                .border(1.dp, c.line, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 13.dp),
        ) {
            if (value.isEmpty()) Text(stringResource(R.string.field_note_hint), style = MaterialTheme.typography.bodyMedium, color = c.muted)
            BasicTextField(
                value = value,
                onValueChange = onValue,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.ink),
                cursorBrush = SolidColor(c.accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
