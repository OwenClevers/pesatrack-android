package com.pesatrack.app.data.budget

import com.pesatrack.app.domain.model.BudgetThreshold
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.domain.repository.BudgetAlertRepository
import com.pesatrack.app.domain.repository.BudgetRepository
import com.pesatrack.app.domain.repository.CategoryRepository
import com.pesatrack.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.time.YearMonth

/**
 * Recomputes one category's budget spend for one month and notifies for any
 * newly-crossed threshold, called after a transaction is saved or imported.
 * Business logic over repository interfaces, no Context of its own -- the
 * caller decides which thresholds are enabled (Settings) and supplies a
 * BudgetAlertNotifier (a no-op fake in tests, a real one backed by
 * NotificationManager in the app).
 */
class BudgetAlertChecker(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val alertStateRepository: BudgetAlertRepository,
    private val notifier: BudgetAlertNotifier
) {

    suspend fun check(categoryId: Long, month: YearMonth, enabledThresholds: Set<BudgetThreshold>) {
        if (enabledThresholds.isEmpty()) return

        val budget = budgetRepository.getBudgets(month).first().firstOrNull { it.categoryId == categoryId }
            ?: return
        if (budget.limit <= 0) return

        val spent = transactionRepository.getTransactions().first()
            .filter {
                it.categoryId == categoryId &&
                    it.type == TransactionType.EXPENSE &&
                    YearMonth.from(it.transactionDate) == month
            }
            .sumOf { it.amount }
        val percent = ((spent / budget.limit) * 100).toInt()

        val alreadyFired = BudgetThreshold.entries
            .filter { alertStateRepository.hasFired(categoryId, month, it) }
            .toSet()

        val toFire = BudgetAlertEvaluator.evaluate(percent, alreadyFired)
            .filter { it in enabledThresholds }
        if (toFire.isEmpty()) return

        val category = categoryRepository.getCategories().first().firstOrNull { it.id == categoryId }
            ?: return

        toFire.forEach { threshold ->
            if (notifier.notify(category, threshold, percent)) {
                alertStateRepository.markFired(categoryId, month, threshold)
            }
        }
    }
}
