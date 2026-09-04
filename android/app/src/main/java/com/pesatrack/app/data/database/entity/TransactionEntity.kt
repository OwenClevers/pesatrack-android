package com.pesatrack.app.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import java.time.LocalDateTime

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["smsCode"], unique = true)]
)
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

    val updatedAt: LocalDateTime,

    // M-Pesa SMS confirmation code (e.g. "QGH7XXXXX1"). Null for non-SMS-imported
    // rows; the unique index lets a re-import skip duplicates by code rather than
    // by re-parsing and comparing content. SQLite treats each NULL as distinct, so
    // multiple manual/non-SMS rows with a null smsCode don't conflict with each other.
    val smsCode: String? = null
)