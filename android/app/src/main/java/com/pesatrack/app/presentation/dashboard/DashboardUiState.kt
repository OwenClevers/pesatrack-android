package com.pesatrack.app.presentation.dashboard

import com.pesatrack.app.domain.model.Transaction

data class DashboardUiState(
    val todaySpending: Double = 0.0,
    val monthIncome: Double = 0.0,
    val remainingBudget: Double? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true
)