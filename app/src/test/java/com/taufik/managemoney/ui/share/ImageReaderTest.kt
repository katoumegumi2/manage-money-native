package com.taufik.managemoney.ui.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.math.max

/** Foto struk (galeri/kamera) harus terbaca → JPEG base64, diperkecil ke maks 1600 px. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ImageReaderTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun jpegFile(w: Int, h: Int): Uri {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        val file = File(context.cacheDir, "struk_${w}x$h.jpg")
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        return Uri.fromFile(file)
    }

    private fun decodedSize(base64: String): Pair<Int, Int> {
        val bytes = Base64.decode(base64, Base64.NO_WRAP)
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return bmp.width to bmp.height
    }

    @Test
    fun smallImage_isRead() {
        val payload = ImageReader.read(context, jpegFile(800, 600))
        assertNotNull("gambar kecil harus terbaca", payload)
        assertEquals("image/jpeg", payload!!.mimeType)
        assertEquals(800 to 600, decodedSize(payload.base64))
    }

    @Test
    fun largeCameraPhoto_isReadAndScaledDown() {
        val payload = ImageReader.read(context, jpegFile(4000, 3000))
        assertNotNull("foto kamera besar harus terbaca", payload)
        val (w, h) = decodedSize(payload!!.base64)
        assertEquals(1600, max(w, h))
        assertTrue(w > h)
    }

    @Test
    fun nonImage_returnsNull() {
        val file = File(context.cacheDir, "bukan_gambar.jpg").apply { writeText("bukan gambar") }
        assertEquals(null, ImageReader.read(context, Uri.fromFile(file)))
    }
}
