package com.pesatrack.app.presentation.dashboard

import com.pesatrack.app.domain.model.Transaction

data class DashboardUiState(

    val todaySpending: Double = 0.0,

    val todayIncome: Double = 0.0,

    val remainingBudget: Double = 0.0,

    val recentTransactions: List<Transaction> = emptyList()
)