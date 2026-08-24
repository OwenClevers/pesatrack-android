package com.pesatrack.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

class DashboardViewModel(
    repository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        repository.getTransactions()
            .map { transactions -> transactions.toDashboardState() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DashboardUiState()
            )

    private fun List<Transaction>.toDashboardState(): DashboardUiState {
        val today = LocalDate.now()
        val thisMonth = YearMonth.from(today)

        val todaySpending = filter {
            it.type == TransactionType.EXPENSE &&
                    it.transactionDate.toLocalDate() == today
        }.sumOf { it.amount }

        val monthIncome = filter {
            it.type == TransactionType.INCOME &&
                    YearMonth.from(it.transactionDate) == thisMonth
        }.sumOf { it.amount }

        return DashboardUiState(
            todaySpending = todaySpending,
            monthIncome = monthIncome,
            remainingBudget = null,
            recentTransactions = sortedByDescending { it.transactionDate }.take(5),
            isLoading = false
        )
    }

    class Factory(
        private val repository: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DashboardViewModel(repository) as T
    }
}