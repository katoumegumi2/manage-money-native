package com.taufik.managemoney.domain.model

/** Akun/sumber dana (bank, e-wallet, tunai). */
data class Account(
    val id: Long = 0,
    val kode: String,
    val nama: String,
    val jenis: AccountType,
    val saldoAwal: Long,
    val urutan: Int,
    val aktif: Boolean = true,
)

/** Saldo akun hasil hitung dari ledger (saldoAwal + masuk − keluar). */
data class AccountBalance(
    val accountId: Long,
    val kode: String,
    val nama: String,
    val jenis: AccountType,
    val saldo: Long,
)
