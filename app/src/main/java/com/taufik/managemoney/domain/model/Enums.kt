package com.taufik.managemoney.domain.model

/** Jenis transaksi. `wire` = nilai yang dipakai di DB & kontrak parser AI. */
enum class TxType(val wire: String) {
    PENGELUARAN("pengeluaran"),
    PEMASUKAN("pemasukan"),
    TRANSFER("transfer");

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
