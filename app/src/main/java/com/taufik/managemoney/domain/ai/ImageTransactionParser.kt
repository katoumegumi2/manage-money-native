package com.taufik.managemoney.domain.ai

import com.taufik.managemoney.domain.model.ParsedTransaction

/** Satu gambar siap kirim ke model vision: base64 (tanpa prefix) + mime. */
data class ImagePayload(val base64: String, val mimeType: String)

/**
 * Boundary AI untuk input GAMBAR (struk/bukti QRIS). Output schema sama dengan parser teks.
 * Implementasi (vision) tersembunyi di `data/ai`; hanya provider vision (Gemini) yang didukung v1.
 */
interface ImageTransactionParser {
    suspend fun parse(images: List<ImagePayload>): Result<List<ParsedTransaction>>
}
