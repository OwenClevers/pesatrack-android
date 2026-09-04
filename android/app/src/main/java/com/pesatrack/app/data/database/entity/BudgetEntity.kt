package com.pesatrack.app.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.YearMonth

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["categoryId", "month"], unique = true)]
)
data class BudgetEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val categoryId: Long,

    val limit: Double,

    val month: YearMonth
)
