package com.pesatrack.app.presentation.budgets

import com.pesatrack.app.domain.model.Category
import java.time.YearMonth

data class BudgetsUiState(
    val month: YearMonth = YearMonth.now(),
    val rows: List<BudgetRow> = emptyList(),
    val isLoading: Boolean = true
)

data class BudgetRow(
    val category: Category,
    val spent: Double,
    val limit: Double
) {
    val percent: Int
        get() = if (limit > 0) (spent / limit * 100).toInt().coerceAtLeast(0) else 0
}
