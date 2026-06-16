package com.taufik.managemoney.data.db

import com.taufik.managemoney.data.db.dao.AccountBalanceRow
import com.taufik.managemoney.data.db.entity.AccountEntity
import com.taufik.managemoney.data.db.entity.TransactionEntity
import com.taufik.managemoney.domain.model.Account
import com.taufik.managemoney.domain.model.AccountBalance
import com.taufik.managemoney.domain.model.AccountType
import com.taufik.managemoney.domain.model.SourceInput
import com.taufik.managemoney.domain.model.Transaction
import com.taufik.managemoney.domain.model.TxType
import java.time.LocalDate

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    kode = kode,
    nama = nama,
    jenis = AccountType.fromWire(jenis),
    saldoAwal = saldoAwal,
    urutan = urutan,
    aktif = aktif,
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    kode = kode,
    nama = nama,
    jenis = jenis.wire,
    saldoAwal = saldoAwal,
    urutan = urutan,
    aktif = aktif,
    updatedAt = System.currentTimeMillis(),
    deletedAt = null,
)

fun AccountBalanceRow.toDomain(): AccountBalance = AccountBalance(
    accountId = accountId,
    kode = kode,
    nama = nama,
    jenis = AccountType.fromWire(jenis),
    saldo = saldo,
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    tipe = TxType.fromWire(tipe),
    nominal = nominal,
    kategori = kategori,
    akunDariId = akunDariId,
    akunKeId = akunKeId,
    tanggal = LocalDate.parse(tanggal),
    catatan = catatan,
    batchId = batchId,
    sumberInput = SourceInput.fromWire(sumberInput),
    createdAt = createdAt,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    tipe = tipe.wire,
    nominal = nominal,
    kategori = kategori,
    akunDariId = akunDariId,
    akunKeId = akunKeId,
    tanggal = tanggal.toString(),
    catatan = catatan,
    batchId = batchId,
    sumberInput = sumberInput.wire,
    createdAt = createdAt,
    updatedAt = System.currentTimeMillis(),
    deletedAt = null,
)
