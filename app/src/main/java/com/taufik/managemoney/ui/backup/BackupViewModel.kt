package com.taufik.managemoney.ui.backup

import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.auth.AuthState
import com.taufik.managemoney.data.auth.AuthStateStore
import com.taufik.managemoney.data.backup.BackupMeta
import com.taufik.managemoney.data.backup.BackupRepository
import com.taufik.managemoney.data.backup.BackupStateStore
import com.taufik.managemoney.data.backup.DriveAuth
import com.taufik.managemoney.data.backup.BackupScheduler
import com.taufik.managemoney.data.backup.DriveAuthManager
import com.taufik.managemoney.data.backup.PassphraseStore
import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    authStateStore: AuthStateStore,
    private val backupStateStore: BackupStateStore,
    private val driveAuthManager: DriveAuthManager,
    private val backupRepository: BackupRepository,
    private val passphraseStore: PassphraseStore,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val authState: StateFlow<AuthState> = authStateStore.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState())
    val meta: StateFlow<BackupMeta> = backupStateStore.meta
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BackupMeta())
    val restoreChecked: StateFlow<Boolean> = backupStateStore.restoreChecked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun hasPassphrase(): Boolean = passphraseStore.has()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    fun messageShown() { _message.value = null }

    /** null = belum dicek; true/false = hasil cek backup di Drive (untuk gate onboarding). */
    private val _remoteExists = MutableStateFlow<Boolean?>(null)
    val remoteExists: StateFlow<Boolean?> = _remoteExists.asStateFlow()

    private val _pendingConsent = MutableStateFlow<IntentSenderRequest?>(null)
    val pendingConsent: StateFlow<IntentSenderRequest?> = _pendingConsent.asStateFlow()

    private val _restartNeeded = MutableStateFlow(false)
    val restartNeeded: StateFlow<Boolean> = _restartNeeded.asStateFlow()

    private var pendingAction: (suspend (String) -> Unit)? = null

    /** Jalankan [action] dengan access token Drive; minta consent dulu bila perlu. */
    private fun withToken(action: suspend (String) -> Unit) {
        viewModelScope.launch {
            _busy.value = true
            when (val auth = driveAuthManager.authorize()) {
                is DriveAuth.Authorized -> { action(auth.accessToken); _busy.value = false }
                is DriveAuth.NeedsConsent -> {
                    pendingAction = action
                    _pendingConsent.value = IntentSenderRequest.Builder(auth.intentSender).build()
                }
                is DriveAuth.Failed -> { _message.value = "Drive: ${auth.message}"; _busy.value = false }
            }
        }
    }

    fun consentLaunched() { _pendingConsent.value = null }

    fun onConsentResult(data: Intent?) {
        val action = pendingAction
        pendingAction = null
        viewModelScope.launch {
            when (val auth = driveAuthManager.resultFromIntent(data)) {
                is DriveAuth.Authorized -> action?.invoke(auth.accessToken)
                else -> _message.value = "Izin Google Drive dibatalkan"
            }
            _busy.value = false
        }
    }

    // --- Gate onboarding ---

    fun checkRemoteBackup() {
        if (_busy.value) return
        withToken { token -> _remoteExists.value = backupRepository.remoteBackupExists(token) }
    }

    fun skipRestore() {
        viewModelScope.launch { backupStateStore.setRestoreChecked(true) }
    }

    fun restore(passphrase: CharArray) {
        withToken { token ->
            backupRepository.restore(token, passphrase)
                .onSuccess {
                    backupStateStore.setRestoreChecked(true)
                    _restartNeeded.value = true
                }
                .onFailure { _message.value = it.message ?: "Pemulihan gagal" }
        }
    }

    /** Aktifkan backup (set passphrase) + cadangkan sekarang + jadwalkan harian. */
    fun enableBackup(passphrase: String) {
        passphraseStore.set(passphrase)
        withToken { token ->
            backupRepository.backup(token)
                .onSuccess {
                    backupStateStore.setRestoreChecked(true)
                    BackupScheduler.scheduleDaily(appContext)
                    _message.value = "Cadangan tersimpan"
                }
                .onFailure { _message.value = it.message ?: "Backup gagal" }
        }
    }

    // --- Pengaturan ---

    fun backupNow() {
        if (!passphraseStore.has()) { _message.value = "Atur passphrase dulu untuk mengaktifkan backup"; return }
        withToken { token ->
            backupRepository.backup(token)
                .onSuccess {
                    BackupScheduler.scheduleDaily(appContext)
                    _message.value = "Cadangan tersimpan"
                }
                .onFailure { _message.value = it.message ?: "Backup gagal" }
        }
    }

    fun setPassphrase(passphrase: String) {
        passphraseStore.set(passphrase)
        _message.value = "Passphrase disimpan"
    }
}
