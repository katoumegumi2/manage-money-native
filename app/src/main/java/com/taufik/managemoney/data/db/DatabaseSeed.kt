package com.taufik.managemoney.data.db

import com.taufik.managemoney.data.db.entity.AccountEntity
import com.taufik.managemoney.domain.model.AccountType

/**
 * Katalog sumber uang populer di Indonesia. Di-seed saat DB pertama dibuat, dan ditambahkan
 * ke DB lama lewat [Migrations.MIGRATION_1_2] (INSERT OR IGNORE per kode unik).
 *
 * `aktif` = true hanya untuk segelintir yang umum dipakai; sisanya nonaktif agar Beranda tidak
 * penuh. User mengaktifkan/menonaktifkan sendiri lewat layar Kelola akun. saldoAwal = 0.
 */
object DatabaseSeed {
    private fun bank(kode: String, nama: String, urutan: Int, aktif: Boolean = false) =
        AccountEntity(kode = kode, nama = nama, jenis = AccountType.BANK.wire, saldoAwal = 0, urutan = urutan, aktif = aktif)

    private fun ewallet(kode: String, nama: String, urutan: Int, aktif: Boolean = false) =
        AccountEntity(kode = kode, nama = nama, jenis = AccountType.EWALLET.wire, saldoAwal = 0, urutan = urutan, aktif = aktif)

    val accounts: List<AccountEntity> = listOf(
        // Bank
        bank("bca", "BCA", 1, aktif = true),
        bank("bni", "BNI", 2, aktif = true),
        bank("bri", "BRI", 3, aktif = true),
        bank("mandiri", "Mandiri", 4, aktif = true),
        bank("bsi", "BSI", 5),
        bank("cimb", "CIMB Niaga", 6),
        bank("permata", "Permata", 7),
        bank("danamon", "Danamon", 8),
        bank("btn", "BTN", 9),
        bank("seabank", "SeaBank", 10),
        bank("jago", "Bank Jago", 11),
        bank("jenius", "Jenius", 12),
        bank("blu", "blu", 13),
        bank("allobank", "Allo Bank", 14),
        bank("bankjatim", "Bank Jatim", 15),
        // E-wallet
        ewallet("gopay", "GoPay", 20, aktif = true),
        ewallet("ovo", "OVO", 21),
        ewallet("dana", "DANA", 22),
        ewallet("shopeepay", "ShopeePay", 23),
        ewallet("linkaja", "LinkAja", 24),
        ewallet("sakuku", "Sakuku", 25),
        ewallet("isaku", "i.saku", 26),
        ewallet("astrapay", "AstraPay", 27),
        // Tunai
        AccountEntity(kode = "tunai", nama = "Tunai", jenis = AccountType.TUNAI.wire, saldoAwal = 0, urutan = 30, aktif = true),
    )
}
