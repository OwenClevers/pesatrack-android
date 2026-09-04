package com.pesatrack.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import java.time.LocalDateTime

@Entity(tableName = "transactions")
data class TransactionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Double,

    val type: TransactionType,

    val categoryId: Long,

    val merchant: String? = null,

    val description: String? = null,

    val transactionDate: LocalDateTime,

    val source: TransactionSource,

    val createdAt: LocalDateTime,

    val updatedAt: LocalDateTime
)