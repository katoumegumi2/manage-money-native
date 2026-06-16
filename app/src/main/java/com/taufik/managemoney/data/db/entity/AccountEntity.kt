package com.taufik.managemoney.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["kode"], unique = true)],
)
data class AccountEntity(
    // Primary key = UUID string (bukan autoincrement) → siap sync multi-device tanpa bentrok id antar HP.
    @PrimaryKey val id: String = "",
    val kode: String,
    val nama: String,
    val jenis: String, // AccountType.wire
    val saldoAwal: Long,
    val urutan: Int,
    val aktif: Boolean = true,
    // Metadata sync: updatedAt = epoch ms perubahan terakhir; deletedAt = tombstone (null = aktif).
    val updatedAt: Long = 0,
    val deletedAt: Long? = null,
)
