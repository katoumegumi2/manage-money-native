package com.taufik.managemoney.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Jenis transaksi. `wire` = nilai yang dipakai di DB & kontrak parser AI. */
@Serializable
enum class TxType(val wire: String) {
    @SerialName("pengeluaran") PENGELUARAN("pengeluaran"),
    @SerialName("pemasukan") PEMASUKAN("pemasukan"),
    @SerialName("transfer") TRANSFER("transfer");

    companion object {
        fun fromWire(value: String): TxType =
            entries.firstOrNull { it.wire == value }
                ?: error("TxType tidak dikenal: $value")
    }
}

/** Jenis akun (untuk ikon & pengelompokan UI). */
enum class AccountType(val wire: String) {
    BANK("bank"),
    EWALLET("ewallet"),
    TUNAI("tunai");

    companion object {
        fun fromWire(value: String): AccountType =
            entries.firstOrNull { it.wire == value }
                ?: error("AccountType tidak dikenal: $value")
    }
}

/** Jejak asal transaksi. */
enum class SourceInput(val wire: String) {
    VOICE("voice"),
    MANUAL("manual");

    companion object {
        fun fromWire(value: String): SourceInput =
            entries.firstOrNull { it.wire == value }
                ?: error("SourceInput tidak dikenal: $value")
    }
}
