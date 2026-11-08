package com.taufik.managemoney.ui.share

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.repo.ParseRepository
import com.taufik.managemoney.domain.ai.ImageTransactionParser
import com.taufik.managemoney.domain.model.ParsedTransaction
import com.taufik.managemoney.ui.navigation.ConfirmPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Konten yang dibagikan ke app lewat share sheet. */
sealed interface SharedContent {
    data class Text(val text: String) : SharedContent
    data class Images(val uris: List<Uri>) : SharedContent
}

sealed interface ShareState {
    data object Loading : ShareState
    data class Ready(val payload: String) : ShareState // ConfirmPayload terenkode
    data object Empty : ShareState
    data class Error(val message: String) : ShareState
}

@HiltViewModel
class ShareReceiverViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parseRepository: ParseRepository,
    private val imageParser: ImageTransactionParser,
) : ViewModel() {

    private val _state = MutableStateFlow<ShareState>(ShareState.Loading)
    val state: StateFlow<ShareState> = _state.asStateFlow()

    private var started = false

    fun process(content: SharedContent) {
        if (started) return
        started = true
        viewModelScope.launch {
            val result: Result<List<ParsedTransaction>> = when (content) {
                is SharedContent.Text -> parseRepository.parse(content.text)
                is SharedContent.Images -> {
                    val images = withContext(Dispatchers.IO) { content.uris.mapNotNull { ImageReader.read(context, it) } }
                    if (images.isEmpty()) Result.failure(IllegalStateException("Gambar tidak bisa dibaca"))
                    else imageParser.parse(images)
                }
            }
            _state.value = result.fold(
                onSuccess = { list ->
                    if (list.isEmpty()) ShareState.Empty
                    else ShareState.Ready(ConfirmPayload.encode(list))
                },
                onFailure = { ShareState.Error(it.message ?: "Gagal memproses") },
            )
        }
    }
}
