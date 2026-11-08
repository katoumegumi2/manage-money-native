package com.taufik.managemoney.data.ai

import com.taufik.managemoney.domain.ai.ChatTurn

/**
 * Maks. jumlah pesan riwayat yang ikut dikirim per pertanyaan (± 20 tanya-jawab).
 * Model sekarang ber-context besar; batas ini menjaga biaya & latensi tetap wajar.
 */
internal const val MAX_HISTORY_MESSAGES = 40

/** Ambil [max] pesan terakhir yang tak kosong (urutan dipertahankan). */
internal fun List<ChatTurn>.recent(max: Int = MAX_HISTORY_MESSAGES): List<ChatTurn> =
    filter { it.text.isNotBlank() }.takeLast(max)

internal fun List<ChatTurn>.toOpenAiMessages(): List<ChatMessage> =
    recent().map { ChatMessage(role = if (it.fromUser) "user" else "assistant", content = it.text) }

internal fun List<ChatTurn>.toGeminiContents(): List<GeminiContent> =
    recent().map { GeminiContent(role = if (it.fromUser) "user" else "model", parts = listOf(GeminiPart(text = it.text))) }
