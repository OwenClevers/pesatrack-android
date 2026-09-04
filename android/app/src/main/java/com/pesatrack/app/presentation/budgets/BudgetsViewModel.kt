package com.pesatrack.app.presentation.budgets

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
import kotlinx.coroutines.launch
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetsViewModel(
    private val budgetRepository: BudgetRepository,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<BudgetsUiState> =
        selectedMonth
            .flatMapLatest { month ->
                combine(
                    budgetRepository.getBudgets(month),
                    transactionRepository.getTransactions(),
                    categoryRepository.getCategories()
                ) { budgets, transactions, categories ->
                    buildState(month, budgets, transactions, categories)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = BudgetsUiState()
            )

    fun onMonthSelected(month: YearMonth) {
        selectedMonth.value = month
    }

    fun saveBudget(budgetId: Long, categoryId: Long, limit: Double) {
        viewModelScope.launch {
            budgetRepository.upsertBudget(
                Budget(id = budgetId, categoryId = categoryId, limit = limit, month = selectedMonth.value)
            )
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budgetId)
        }
    }

    private fun buildState(
        month: YearMonth,
        budgets: List<Budget>,
        transactions: List<Transaction>,
        categories: List<Category>
    ): BudgetsUiState {
        val categoriesById = categories.associateBy { it.id }

        val rows = budgets
            .sortedBy { it.categoryId }
            .map { budget ->
                val spent = transactions
                    .filter {
                        it.categoryId == budget.categoryId &&
                                it.type == TransactionType.EXPENSE &&
                                YearMonth.from(it.transactionDate) == month
                    }
                    .sumOf { it.amount }

                BudgetRow(
                    budgetId = budget.id,
                    category = categoriesById[budget.categoryId] ?: Category.unknown(budget.categoryId),
                    spent = spent,
                    limit = budget.limit
                )
            }

        return BudgetsUiState(month = month, rows = rows, categories = categories, isLoading = false)
    }

    class Factory(
        private val budgetRepository: BudgetRepository,
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BudgetsViewModel(budgetRepository, transactionRepository, categoryRepository) as T
    }
}
