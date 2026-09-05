package com.pesatrack.app.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.YearMonth

@Entity(
    tableName = "budget_alerts",
    indices = [Index(value = ["categoryId", "month", "threshold"], unique = true)]
)
data class BudgetAlertEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val categoryId: Long,

    val month: YearMonth,

    // Stored as the enum's name (e.g. "WARNING") via Converters, matching
    // TransactionType/TransactionSource elsewhere in this database.
    val threshold: String
)
