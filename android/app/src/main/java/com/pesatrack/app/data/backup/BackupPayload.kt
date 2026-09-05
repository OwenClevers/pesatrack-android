package com.pesatrack.app.data.backup

import com.pesatrack.app.domain.model.Budget
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import java.time.LocalDateTime

// A full snapshot of the database for backup/restore. Plain Kotlin -- no Room
// dependency -- so it's testable as a plain JVM unit like the domain models it
// carries.
data class BackupPayload(
    val schemaVersion: Int,
    val exportedAt: LocalDateTime,
    val categories: List<Category>,
    val transactions: List<Transaction>,
    val budgets: List<Budget>
)
