package com.taufik.managemoney.data.ai

import com.taufik.managemoney.domain.model.Categories
import com.taufik.managemoney.domain.model.ParsedTransaction
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Gagal saat membersihkan/parse/validasi balasan model. UI → fallback manual (brief §3.5). */
class ParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Validator §3.5 (sama untuk semua provider):
 * bersihkan fence ```json → parse JSON array (objek pembungkus dibuka) → validasi tiap elemen
 * (kategori sesuai tipe, nominal ≥ 0, tanggal YYYY-MM-DD). Akun dikanonikalisasi ke kode
 * (case-insensitive); akun di luar daftar → null + perlu dicek (bukan error, mis. bank eksternal).
 * Balasan `[]` → list kosong (sukses). Gagal apa pun → [Result.failure].
 */
object ParserValidator {
    private val json = Json { ignoreUnknownKeys = true }
    private val isoDate = Regex("""\d{4}-\d{2}-\d{2}""")

    fun parse(rawText: String, accountCodes: Set<String>): Result<List<ParsedTransaction>> {
        val cleaned = stripFences(rawText)
        if (cleaned.isEmpty()) return Result.failure(ParseException("Balasan kosong"))

        val list = try {
            json.decodeFromJsonElement<List<ParsedTransaction>>(unwrapToArray(json.parseToJsonElement(cleaned)))
        } catch (e: Exception) {
            return Result.failure(ParseException("Balasan bukan JSON yang valid", e))
        }

        // Kanonikalisasi kode akun: model bisa balas beda kapitalisasi (mis. "SeaBank" vs "seabank").
        // Akun yang tak ada di daftar (mis. bank eksternal/teman pada transfer masuk) BUKAN error —
        // jadikan null & tandai perlu dicek agar transaksi tetap masuk Konfirmasi untuk dilengkapi.
        val canonicalByLower = accountCodes.associateBy { it.lowercase() }
        val normalized = ArrayList<ParsedTransaction>(list.size)
        list.forEachIndexed { i, tx ->
            validate(tx)?.let { return failAt(i, it) }
            val dari = tx.akunDari?.lowercase()?.let { canonicalByLower[it] }
            val ke = tx.akunKe?.lowercase()?.let { canonicalByLower[it] }
            val akunTakDikenal = (tx.akunDari != null && dari == null) || (tx.akunKe != null && ke == null)
            normalized += tx.copy(akunDari = dari, akunKe = ke, perluDicek = tx.perluDicek || akunTakDikenal)
        }
        return Result.success(normalized)
    }

    /**
     * Kontraknya array, tapi mode `response_format: json_object` (OpenAI-compatible, mis. DeepSeek)
     * memaksa balasan berupa OBJEK — model lalu membungkus array, mis. `{"data":[...]}` atau
     * `{"transaksi":[...]}`, atau mengirim satu transaksi tanpa array. Normalisasi ke array.
     */
    private fun unwrapToArray(element: JsonElement): JsonArray = when (element) {
        is JsonArray -> element
        is JsonObject -> when {
            "tipe" in element -> JsonArray(listOf(element))
            else -> element.values.firstOrNull { it is JsonArray } as? JsonArray
                ?: throw ParseException("Objek JSON tanpa daftar transaksi")
        }
        else -> throw ParseException("Balasan bukan array JSON")
    }

    private fun failAt(index: Int, reason: String): Result<List<ParsedTransaction>> =
        Result.failure(ParseException("Transaksi #${index + 1}: $reason"))

    private fun validate(tx: ParsedTransaction): String? {
        if (tx.nominal < 0) return "nominal negatif"
        if (tx.kategori !in Categories.forType(tx.tipe)) {
            return "kategori '${tx.kategori}' tak sesuai tipe ${tx.tipe.wire}"
        }
        if (!isoDate.matches(tx.tanggal)) return "tanggal '${tx.tanggal}' bukan format YYYY-MM-DD"
        try {
            LocalDate.parse(tx.tanggal)
        } catch (e: DateTimeParseException) {
            return "tanggal '${tx.tanggal}' tidak valid"
        }
        return null
    }

    private fun stripFences(text: String): String {
        var t = text.trim()
        if (t.startsWith("```")) {
            t = t.removePrefix("```")
            if (t.startsWith("json")) t = t.removePrefix("json")
            if (t.endsWith("```")) t = t.removeSuffix("```")
            t = t.trim()
        }
        return t
    }
}
