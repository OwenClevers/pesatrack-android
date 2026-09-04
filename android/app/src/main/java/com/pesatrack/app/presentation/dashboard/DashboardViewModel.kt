package com.pesatrack.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesatrack.app.domain.model.Budget
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.domain.repository.BudgetRepository
import com.pesatrack.app.domain.repository.CategoryRepository
import com.pesatrack.app.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    budgetRepository: BudgetRepository
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<DashboardUiState> =
        selectedMonth
            .flatMapLatest { month ->
                combine(
                    transactionRepository.getTransactions(),
                    categoryRepository.getCategories(),
                    budgetRepository.getBudgets(month)
                ) { transactions, categories, budgets ->
                    transactions.toDashboardState(month, categories, budgets)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DashboardUiState()
            )

    fun onMonthSelected(month: YearMonth) {
        selectedMonth.value = month
    }

    private fun List<Transaction>.toDashboardState(
        month: YearMonth,
        categories: List<Category>,
        budgets: List<Budget>
    ): DashboardUiState {
        val today = LocalDate.now()

        // "Today" always means the real calendar day, regardless of which
        // month is being browsed elsewhere on this screen.
        val todaySpending = filter {
            it.type == TransactionType.EXPENSE &&
                    it.transactionDate.toLocalDate() == today
        }.sumOf { it.amount }

        val monthIncome = filter {
            it.type == TransactionType.INCOME &&
                    YearMonth.from(it.transactionDate) == month
        }.sumOf { it.amount }

        val remainingBudget = if (budgets.isEmpty()) {
            null
        } else {
            val totalLimit = budgets.sumOf { it.limit }
            val totalSpent = budgets.sumOf { budget ->
                filter {
                    it.type == TransactionType.EXPENSE &&
                            it.categoryId == budget.categoryId &&
                            YearMonth.from(it.transactionDate) == month
                }.sumOf { it.amount }
            }
            totalLimit - totalSpent
        }

        val recentTransactions = filter { YearMonth.from(it.transactionDate) == month }
            .sortedByDescending { it.transactionDate }
            .take(5)

        return DashboardUiState(
            month = month,
            todaySpending = todaySpending,
            monthIncome = monthIncome,
            remainingBudget = remainingBudget,
            recentTransactions = recentTransactions,
            categoriesById = categories.associateBy { it.id },
            isLoading = false
        )
    }

    class Factory(
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository,
        private val budgetRepository: BudgetRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DashboardViewModel(transactionRepository, categoryRepository, budgetRepository) as T
    }
}
