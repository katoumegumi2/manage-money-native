package com.taufik.managemoney.ui.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taufik.managemoney.data.repo.ParseRepository
import com.taufik.managemoney.domain.ai.ImagePayload
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
import java.io.ByteArrayOutputStream
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
                    val images = withContext(Dispatchers.IO) { content.uris.mapNotNull(::readImage) }
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

    /** Baca Uri gambar → kompres (maks 1024px, JPEG 80) → base64. */
    private fun readImage(uri: Uri): ImagePayload? = runCatching {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        val scaled = scaleDown(bmp, 1024)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
        ImagePayload(Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP), "image/jpeg")
    }.getOrNull()

    private fun scaleDown(bmp: Bitmap, maxDim: Int): Bitmap {
        val w = bmp.width
        val h = bmp.height
        val largest = maxOf(w, h)
        if (largest <= maxDim) return bmp
        val ratio = maxDim.toFloat() / largest
        return Bitmap.createScaledBitmap(bmp, (w * ratio).toInt(), (h * ratio).toInt(), true)
    }
}
