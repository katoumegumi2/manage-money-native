package com.taufik.managemoney.ui.manualentry

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import com.taufik.managemoney.ui.components.PrimaryPillButton
import com.taufik.managemoney.ui.components.SubScreenHeader
import com.taufik.managemoney.ui.components.appCard
import com.taufik.managemoney.ui.components.screenBackground

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taufik.managemoney.R
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

    Column(
        Modifier.fillMaxSize().screenBackground().statusBarsPadding().navigationBarsPadding().imePadding(),
    ) {
        SubScreenHeader(stringResource(R.string.action_add_manual), onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                Modifier.fillMaxWidth().appCard(androidx.compose.foundation.shape.RoundedCornerShape(26.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TipeSegmented(selected = draft.tipe, onSelect = viewModel::setTipe)
                NominalField(value = draft.nominal, onValue = viewModel::setNominal)
                if (draft.tipe != TxType.TRANSFER) {
                    KategoriPicker(tipe = draft.tipe, selected = draft.kategori, onSelect = viewModel::setKategori)
                }
                when (draft.tipe) {
                    TxType.PENGELUARAN -> AccountPicker(stringResource(R.string.field_account_from), state.accounts, draft.akunDariId, viewModel::setAkunDari)
                    TxType.PEMASUKAN -> AccountPicker(stringResource(R.string.field_account_to), state.accounts, draft.akunKeId, viewModel::setAkunKe)
                    TxType.TRANSFER -> TransferAccounts(state.accounts, draft.akunDariId, draft.akunKeId, viewModel::setAkunDari, viewModel::setAkunKe, fromError = false, toError = false)
                }
                TanggalField(value = draft.tanggal, onValue = viewModel::setTanggal)
                CatatanField(value = draft.catatan, onValue = viewModel::setCatatan)
            }
        }
        PrimaryPillButton(
            stringResource(R.string.manual_continue),
            onClick = { onContinue(viewModel.buildPayload()) },
            enabled = state.canContinue,
            icon = R.drawable.sol_arrow_right,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}
