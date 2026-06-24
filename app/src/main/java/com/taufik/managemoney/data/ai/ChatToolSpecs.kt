package com.taufik.managemoney.data.ai

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Spesifikasi tool untuk Tanya AI (function calling). Provider-neutral; di-render ke
 * `functionDeclarations` (Gemini) atau `tools` (OpenAI). Eksekusinya ada di [ChatToolLogic].
 */
internal data class ChatToolSpec(
    val name: String,
    val description: String,
    val parameters: JsonObject,
)

/** Helper: properti JSON Schema bertipe string. */
private fun strProp(desc: String) = buildJsonObject {
    put("type", "string")
    put("description", desc)
}

private fun objSchema(
    properties: JsonObject,
    required: List<String> = emptyList(),
) = buildJsonObject {
    put("type", "object")
    put("properties", properties)
    put(
        "required",
        buildJsonArray { required.forEach { add(it) } },
    )
}

internal val CHAT_TOOL_SPECS: List<ChatToolSpec> = listOf(
    ChatToolSpec(
        name = "saldo_akun",
        description = "Saldo semua akun saat ini (bank, e-wallet, tunai) + total. Tanpa argumen.",
        parameters = objSchema(buildJsonObject {}),
    ),
    ChatToolSpec(
        name = "ringkas_keuangan",
        description = "Total pemasukan, pengeluaran, net, dan pengeluaran per kategori dalam rentang tanggal " +
            "(inklusif). Pakai untuk pertanyaan agregat seperti 'pengeluaran bulan ini'.",
        parameters = objSchema(
            properties = buildJsonObject {
                put("dari_tanggal", strProp("Tanggal mulai YYYY-MM-DD (inklusif)."))
                put("sampai_tanggal", strProp("Tanggal akhir YYYY-MM-DD (inklusif)."))
            },
            required = listOf("dari_tanggal", "sampai_tanggal"),
        ),
    ),
    ChatToolSpec(
        name = "cari_transaksi",
        description = "Cari/saring transaksi dari SELURUH riwayat. Semua argumen opsional & bisa digabung. " +
            "Pakai 'kata_kunci' untuk mencocokkan catatan/kategori/nama akun — termasuk untuk utang/piutang " +
            "(mis. kata 'utang', 'pinjam', atau nama orang). Hasil diurutkan terbaru dulu.",
        parameters = objSchema(
            properties = buildJsonObject {
                put("kata_kunci", strProp("Cocokkan ke catatan, kategori, atau nama akun (case-insensitive)."))
                put("dari_tanggal", strProp("Batas awal tanggal YYYY-MM-DD (inklusif)."))
                put("sampai_tanggal", strProp("Batas akhir tanggal YYYY-MM-DD (inklusif)."))
                put("tipe", strProp("Filter tipe: pengeluaran | pemasukan | transfer."))
                put("kategori", strProp("Filter kategori persis, mis. makanan."))
                put("akun", strProp("Filter akun (nama/kode), mis. gopay."))
                put(
                    "limit",
                    buildJsonObject {
                        put("type", "integer")
                        put("description", "Maksimum transaksi dikembalikan (default 50, maks 100).")
                    },
                )
            },
        ),
    ),
)

/** Render spesifikasi → deklarasi tool Gemini. */
internal fun geminiFunctionDeclarations(): List<GeminiFunctionDeclaration> =
    CHAT_TOOL_SPECS.map { GeminiFunctionDeclaration(it.name, it.description, it.parameters) }

/** Render spesifikasi → tools OpenAI-compatible. */
internal fun openAiTools(): List<OpenAiTool> =
    CHAT_TOOL_SPECS.map { OpenAiTool(function = OpenAiFunctionDef(it.name, it.description, it.parameters)) }
