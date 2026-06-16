package com.taufik.managemoney.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["akunDariId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["akunKeId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("akunDariId"),
        Index("akunKeId"),
        Index("batchId"),
        Index("tanggal"),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipe: String, // TxType.wire
    val nominal: Long,
    val kategori: String,
    val akunDariId: Long?,
    val akunKeId: Long?,
    val tanggal: String, // ISO YYYY-MM-DD
    val catatan: String,
    val batchId: String?,
    val sumberInput: String, // SourceInput.wire
    val createdAt: Long,
)
