package com.taufik.managemoney.ui.manualentry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.domain.model.TxType
import com.taufik.managemoney.ui.components.AccountPicker
import com.taufik.managemoney.ui.components.CatatanField
import com.taufik.managemoney.ui.components.KategoriPicker
import com.taufik.managemoney.ui.components.NominalField
import com.taufik.managemoney.ui.components.TanggalField
import com.taufik.managemoney.ui.components.TipeSegmented
import com.taufik.managemoney.ui.components.TransferAccounts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    onBack: () -> Unit,
    onContinue: (payload: String) -> Unit,
    viewModel: ManualEntryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val draft = state.draft

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah manual") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = { onContinue(viewModel.buildPayload()) },
                    enabled = state.canContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text("Lanjut")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TipeSegmented(selected = draft.tipe, onSelect = viewModel::setTipe)

            NominalField(value = draft.nominal, onValue = viewModel::setNominal)

            if (draft.tipe != TxType.TRANSFER) {
                KategoriPicker(
                    tipe = draft.tipe,
                    selected = draft.kategori,
                    onSelect = viewModel::setKategori,
                )
            }

            when (draft.tipe) {
                TxType.PENGELUARAN -> AccountPicker(
                    label = "Dari",
                    accounts = state.accounts,
                    selectedId = draft.akunDariId,
                    onSelect = viewModel::setAkunDari,
                )
                TxType.PEMASUKAN -> AccountPicker(
                    label = "Ke",
                    accounts = state.accounts,
                    selectedId = draft.akunKeId,
                    onSelect = viewModel::setAkunKe,
                )
                TxType.TRANSFER -> TransferAccounts(
                    accounts = state.accounts,
                    fromId = draft.akunDariId,
                    toId = draft.akunKeId,
                    onFrom = viewModel::setAkunDari,
                    onTo = viewModel::setAkunKe,
                    fromError = false,
                    toError = false,
                )
            }

            TanggalField(value = draft.tanggal, onValue = viewModel::setTanggal)
            CatatanField(value = draft.catatan, onValue = viewModel::setCatatan)
        }
    }
}
