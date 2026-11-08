package com.taufik.managemoney.domain.model

/** Kategori per tipe transaksi (lihat CLAUDE.md / brief). Transfer selalu "lainnya". */
object Categories {
    val PENGELUARAN = listOf(
        "makanan", "belanja", "transport", "bbm", "tagihan", "langganan",
        "hiburan", "game", "kesehatan", "perawatan", "laundry", "pendidikan",
        "sosial", "keluarga", "perjalanan", "pinjamkan", "bayar_utang",
        "admin", "investasi", "lainnya",
    )
    val PEMASUKAN = listOf(
        "gaji", "usaha", "bonus", "sampingan", "reimburse", "pengembalian",
        "pinjaman", "hadiah", "cashback", "refund", "penjualan", "investasi",
        "transfer", "penyesuaian", "lainnya",
    )
    val TRANSFER = listOf("lainnya")

    fun forType(tipe: TxType): List<String> = when (tipe) {
        TxType.PENGELUARAN -> PENGELUARAN
        TxType.PEMASUKAN -> PEMASUKAN
        TxType.TRANSFER -> TRANSFER
    }

    /** Kategori default ketika berganti tipe. */
    fun defaultFor(tipe: TxType): String = when (tipe) {
        TxType.PENGELUARAN -> "makanan"
        TxType.PEMASUKAN -> "gaji"
        TxType.TRANSFER -> "lainnya"
    }
}
