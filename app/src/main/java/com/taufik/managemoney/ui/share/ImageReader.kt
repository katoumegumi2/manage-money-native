package com.taufik.managemoney.ui.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.taufik.managemoney.domain.ai.ImagePayload
import java.io.ByteArrayOutputStream

/** Baca Uri gambar → kompres (maks 1024px, JPEG 80) → base64. Dipakai share sheet & pilih foto manual. */
internal object ImageReader {
    fun read(context: Context, uri: Uri): ImagePayload? = runCatching {
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
