package com.taufik.managemoney.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.taufik.managemoney.core.formatRupiah
import com.taufik.managemoney.domain.model.Account
import com.taufik.managemoney.domain.model.Categories
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.util.accountVisual
import com.taufik.managemoney.ui.util.kategoriLabel
import com.taufik.managemoney.ui.util.txTypeLabel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeId = Locale.forLanguageTag("id-ID")
private val tanggalFormatter = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", localeId)

/** Segmen pemilih tipe transaksi (dipakai di Manual entry). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipeSegmented(
    selected: TxType,
    onSelect: (TxType) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        TxType.entries.forEachIndexed { index, tipe ->
            SegmentedButton(
                selected = tipe == selected,
                onClick = { onSelect(tipe) },
                shape = SegmentedButtonDefaults.itemShape(index, TxType.entries.size),
            ) {
                Text(txTypeLabel(tipe))
            }
        }
    }
}

/** Input nominal rupiah. Menyimpan Long; menampilkan "Rp 1.234.567" sebagai supporting text. */
@Composable
fun NominalField(
    value: Long,
    onValue: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = if (value == 0L) "" else value.toString(),
        onValueChange = { raw ->
            val digits = raw.filter { it.isDigit() }.take(15)
            onValue(digits.toLongOrNull() ?: 0L)
        },
        label = { Text("Nominal") },
        prefix = { Text("Rp ") },
        supportingText = if (value > 0) {
            { Text(formatRupiah(value)) }
        } else {
            null
        },
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Dropdown kategori sesuai tipe. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KategoriPicker(
    tipe: TxType,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = Categories.forType(tipe)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = kategoriLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text("Kategori") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(kategoriLabel(opt)) },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    },
                )
            }
        }
    }
}

/** Picker akun (WAJIB). [label] = "Dari" / "Ke". Menampilkan badge brand + nama. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPicker(
    label: String,
    accounts: List<Account>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = accounts.firstOrNull { it.id == selectedId }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selected?.nama ?: "",
            onValueChange = {},
            readOnly = true,
            isError = isError,
            label = { Text(label) },
            placeholder = { Text("Pilih akun") },
            leadingIcon = selected?.let { acc ->
                {
                    val v = accountVisual(acc.kode, acc.jenis)
                    AccountBadge(abbr = v.abbr, color = v.color, size = 26)
                }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { acc ->
                val v = accountVisual(acc.kode, acc.jenis)
                DropdownMenuItem(
                    text = { Text(acc.nama) },
                    leadingIcon = { AccountBadge(abbr = v.abbr, color = v.color, size = 26) },
                    onClick = {
                        onSelect(acc.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

/** Picker "Dari" + "Ke" untuk transfer (harus beda akun). */
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
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AccountPicker(
            label = "Dari",
            accounts = accounts,
            selectedId = fromId,
            onSelect = onFrom,
            isError = fromError,
            modifier = Modifier.weight(1f),
        )
        AccountPicker(
            label = "Ke",
            accounts = accounts,
            selectedId = toId,
            onSelect = onTo,
            isError = toError,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Field tanggal — tap membuka date picker. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TanggalField(
    value: LocalDate,
    onValue: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value.format(tanggalFormatter),
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Tanggal") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDialog = true },
        )
    }
    if (showDialog) {
        val zone = ZoneId.of("Asia/Jakarta")
        val state = rememberDatePickerState(
            initialSelectedDateMillis = value.atStartOfDay(zone).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onValue(Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate())
                    }
                    showDialog = false
                }) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Batal") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

/** Field catatan opsional. */
@Composable
fun CatatanField(
    value: String,
    onValue: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text("Catatan") },
        placeholder = { Text("Opsional") },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
    )
}
