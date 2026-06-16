package com.taufik.managemoney.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["kode"], unique = true)],
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kode: String,
    val nama: String,
    val jenis: String, // AccountType.wire
    val saldoAwal: Long,
    val urutan: Int,
    val aktif: Boolean = true,
)
