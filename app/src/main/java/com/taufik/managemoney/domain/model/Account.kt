package com.taufik.managemoney.domain.model

/** Akun/sumber dana (bank, e-wallet, tunai). id = UUID (kosong = belum tersimpan). */
data class Account(
    val id: String = "",
    val kode: String,
    val nama: String,
    val jenis: AccountType,
    val saldoAwal: Long,
    val urutan: Int,
    val aktif: Boolean = true,
)

/** Saldo akun hasil hitung dari ledger (saldoAwal + masuk − keluar). */
data class AccountBalance(
    val accountId: String,
    val kode: String,
    val nama: String,
    val jenis: AccountType,
    val saldo: Long,
)
