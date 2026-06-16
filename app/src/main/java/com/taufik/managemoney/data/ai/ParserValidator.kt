package com.taufik.managemoney.data.ai

import com.taufik.managemoney.domain.model.Categories
import com.taufik.managemoney.domain.model.ParsedTransaction
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Gagal saat membersihkan/parse/validasi balasan model. UI → fallback manual (brief §3.5). */
class ParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Validator §3.5 (sama untuk semua provider):
 * bersihkan fence ```json → parse JSON array → validasi tiap elemen
 * (kategori sesuai tipe, nominal ≥ 0, tanggal YYYY-MM-DD, akun non-null ada di daftar).
 * Balasan `[]` → list kosong (sukses). Gagal apa pun → [Result.failure].
 */
object ParserValidator {
    private val json = Json { ignoreUnknownKeys = true }
    private val isoDate = Regex("""\d{4}-\d{2}-\d{2}""")

    fun parse(rawText: String, accountCodes: Set<String>): Result<List<ParsedTransaction>> {
        val cleaned = stripFences(rawText)
        if (cleaned.isEmpty()) return Result.failure(ParseException("Balasan kosong"))

        val list = try {
            json.decodeFromString<List<ParsedTransaction>>(cleaned)
        } catch (e: Exception) {
            return Result.failure(ParseException("Balasan bukan JSON yang valid", e))
        }

        list.forEachIndexed { i, tx ->
            validate(tx, accountCodes)?.let {
                return Result.failure(ParseException("Transaksi #${i + 1}: $it"))
            }
        }
        return Result.success(list)
    }

    private fun validate(tx: ParsedTransaction, accountCodes: Set<String>): String? {
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
        tx.akunDari?.let { if (it !in accountCodes) return "akun_dari '$it' tak dikenal" }
        tx.akunKe?.let { if (it !in accountCodes) return "akun_ke '$it' tak dikenal" }
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
