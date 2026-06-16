package com.taufik.managemoney.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.auth.AuthState
import com.taufik.managemoney.data.auth.AuthStateStore
import com.taufik.managemoney.data.auth.GoogleAuthManager
import com.taufik.managemoney.data.auth.SignInError
import com.taufik.managemoney.data.backup.BackupScheduler
import com.taufik.managemoney.data.backup.BackupStateStore
import com.taufik.managemoney.data.backup.PassphraseStore
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
class AuthViewModel @Inject constructor(
    private val authStateStore: AuthStateStore,
    private val googleAuthManager: GoogleAuthManager,
    private val passphraseStore: PassphraseStore,
    private val backupStateStore: BackupStateStore,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val state: StateFlow<AuthState> = authStateStore.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AuthState(),
    )

    val isLoginConfigured: Boolean get() = googleAuthManager.isConfigured

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    /** Pesan sekali-tampil (toast). UI memanggil [messageShown] setelah menampilkan. */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    fun messageShown() { _message.value = null }

    /** [activityContext] HARUS context Activity (Credential Manager menampilkan UI). */
    fun signIn(activityContext: Context) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            googleAuthManager.signIn(activityContext)
                .onSuccess { acc ->
                    backupStateStore.setRestoreChecked(false) // akun baru tertaut → cek cadangan Drive
                    authStateStore.setSignedIn(acc.email, acc.name)
                }
                .onFailure { e ->
                    _message.value = when (e) {
                        is SignInError.Cancelled -> null // batal: diam
                        is SignInError.NoAccount -> "Tidak ada akun Google di perangkat"
                        is SignInError.NotConfigured -> "Login Google belum dikonfigurasi"
                        else -> "Login gagal: ${e.message}"
                    }
                }
            _busy.value = false
        }
    }

    fun skipBackup() {
        viewModelScope.launch { authStateStore.setSkipBackup() }
    }

    fun signOut() {
        viewModelScope.launch {
            BackupScheduler.cancel(appContext)
            passphraseStore.clear()
            backupStateStore.setRestoreChecked(false) // login lagi → cek ulang cadangan
            authStateStore.clearAccount()
        }
    }
}
