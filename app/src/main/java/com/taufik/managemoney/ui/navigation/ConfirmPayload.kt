package com.taufik.managemoney.ui.navigation

import android.util.Base64
import com.taufik.managemoney.domain.model.ParsedTransaction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Encoding `List<ParsedTransaction>` untuk dioper sebagai argumen rute Konfirmasi.
 * Pakai Base64 URL-safe (tanpa '/', '+', '=') agar aman jadi satu segmen path.
 */
object ConfirmPayload {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private const val FLAGS = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING

    fun encode(items: List<ParsedTransaction>): String =
        Base64.encodeToString(json.encodeToString(items).toByteArray(Charsets.UTF_8), FLAGS)

    fun decode(arg: String): List<ParsedTransaction> =
        json.decodeFromString(String(Base64.decode(arg, FLAGS), Charsets.UTF_8))
}
