package com.taufik.managemoney.data.db

import com.taufik.managemoney.data.db.entity.AccountEntity
import com.taufik.managemoney.domain.model.AccountType

/**
 * 6 akun bawaan, di-seed saat DB pertama kali dibuat. saldoAwal = 0 (user mengisi lewat Kelola akun).
 * Lihat desain-data-keuangan.md.
 */
object DatabaseSeed {
    val accounts: List<AccountEntity> = listOf(
        AccountEntity(kode = "bni", nama = "BNI", jenis = AccountType.BANK.wire, saldoAwal = 0, urutan = 1),
        AccountEntity(kode = "mandiri", nama = "Mandiri", jenis = AccountType.BANK.wire, saldoAwal = 0, urutan = 2),
        AccountEntity(kode = "seabank", nama = "SeaBank", jenis = AccountType.BANK.wire, saldoAwal = 0, urutan = 3),
        AccountEntity(kode = "gopay", nama = "GoPay", jenis = AccountType.EWALLET.wire, saldoAwal = 0, urutan = 4),
        AccountEntity(kode = "shopeepay", nama = "ShopeePay", jenis = AccountType.EWALLET.wire, saldoAwal = 0, urutan = 5),
        AccountEntity(kode = "tunai", nama = "Tunai", jenis = AccountType.TUNAI.wire, saldoAwal = 0, urutan = 6),
    )
}
