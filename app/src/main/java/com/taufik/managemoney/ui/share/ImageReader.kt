package com.taufik.managemoney.ui.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import com.taufik.managemoney.domain.ai.ImagePayload
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Baca Uri gambar (galeri, share sheet, atau foto kamera) → JPEG base64 untuk AI vision.
 * - Decode bertahap (inSampleSize) agar foto kamera 12–50 MP tak bikin kehabisan memori.
 * - Ikuti rotasi EXIF agar struk tak terbaca miring.
 * - Maks [MAX_DIM] px & JPEG 85: tulisan kecil di struk tetap terbaca, ukuran kiriman wajar.
 */
internal object ImageReader {
    private const val MAX_DIM = 1600

    fun read(context: Context, uri: Uri): ImagePayload? = runCatching {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        // Catatan: dengan inJustDecodeBounds, decodeStream SELALU mengembalikan null (hanya isi outWidth/
        // outHeight) — jadi jangan `?: return null` pada hasilnya; cukup pastikan stream terbuka.
        val stream = resolver.openInputStream(uri) ?: return null
        stream.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_DIM) sample *= 2
        val bmp = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        val rotation = runCatching {
            resolver.openInputStream(uri)?.use { ExifInterface(it).rotationDegrees() } ?: 0
        }.getOrDefault(0)
        val upright = if (rotation != 0) {
            Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(rotation.toFloat()) }, true)
        } else {
            bmp
        }
        val scaled = scaleDown(upright, MAX_DIM)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
        ImagePayload(Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP), "image/jpeg")
    }.getOrNull()

    private fun ExifInterface.rotationDegrees(): Int = when (getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }

    private fun scaleDown(bmp: Bitmap, maxDim: Int): Bitmap {
        val largest = max(bmp.width, bmp.height)
        if (largest <= maxDim) return bmp
        val ratio = maxDim.toFloat() / largest
        return Bitmap.createScaledBitmap(bmp, (bmp.width * ratio).toInt(), (bmp.height * ratio).toInt(), true)
    }
}
