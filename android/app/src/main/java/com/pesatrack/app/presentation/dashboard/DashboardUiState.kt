package com.pesatrack.app.presentation.dashboard

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import java.time.YearMonth

data class DashboardUiState(
    val month: YearMonth = YearMonth.now(),
    val todaySpending: Double = 0.0,
    val monthIncome: Double = 0.0,
    val remainingBudget: Double? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val categoriesById: Map<Long, Category> = emptyMap(),
    val isLoading: Boolean = true
)
