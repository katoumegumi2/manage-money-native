package com.taufik.managemoney.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Kontrak hasil parser AI (lihat brief §3.2). Akun memakai **kode** string (mis. "bni") atau null.
 * Di Phase 3 diisi oleh form Manual entry; di Phase 4 diisi oleh `TransactionParser`.
 * Layar Konfirmasi menerima `List<ParsedTransaction>`, lalu me-resolve kode → id akun saat simpan.
 */
@Serializable
data class ParsedTransaction(
    val tipe: TxType,
    val nominal: Long,
    val kategori: String,
    @SerialName("akun_dari") val akunDari: String? = null,
    @SerialName("akun_ke") val akunKe: String? = null,
    val tanggal: String, // YYYY-MM-DD
    val catatan: String = "",
    @SerialName("perlu_dicek") val perluDicek: Boolean = false,
)
