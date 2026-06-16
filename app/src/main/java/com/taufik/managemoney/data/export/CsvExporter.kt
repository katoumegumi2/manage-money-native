package com.taufik.managemoney.data.export

import com.taufik.managemoney.domain.model.Transaction

/**
 * Serialisasi transaksi → CSV (RFC 4180). Akun memakai kode. Murni → mudah dites.
 * Kolom: tanggal, tipe, nominal, kategori, akun_dari, akun_ke, catatan.
 */
object CsvExporter {
    private val HEADER = listOf("tanggal", "tipe", "nominal", "kategori", "akun_dari", "akun_ke", "catatan")

    fun export(transactions: List<Transaction>, kodeById: Map<Long, String>): String {
        val sb = StringBuilder()
        sb.append(HEADER.joinToString(",")).append("\r\n")
        transactions.forEach { t ->
            val row = listOf(
                t.tanggal.toString(),
                t.tipe.wire,
                t.nominal.toString(),
                t.kategori,
                t.akunDariId?.let { kodeById[it] } ?: "",
                t.akunKeId?.let { kodeById[it] } ?: "",
                t.catatan,
            )
            sb.append(row.joinToString(",") { escape(it) }).append("\r\n")
        }
        return sb.toString()
    }

    private fun escape(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
}
