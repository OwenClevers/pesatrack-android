package com.pesatrack.app.domain.repository

import com.pesatrack.app.domain.model.BudgetThreshold
import java.time.YearMonth

// Tracks which (category, month, threshold) alerts have already fired, so
// BudgetAlertChecker notifies once per category per month per threshold
// rather than on every transaction, and doesn't re-notify after a restart.
interface BudgetAlertRepository {

    suspend fun hasFired(categoryId: Long, month: YearMonth, threshold: BudgetThreshold): Boolean

    suspend fun markFired(categoryId: Long, month: YearMonth, threshold: BudgetThreshold)
}
