package com.pesatrack.app.presentation.dailyspending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.domain.repository.CategoryRepository
import com.pesatrack.app.domain.repository.TransactionRepository
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

const val DAILY_SPENDING_PERIOD_DAYS = 30

class DailySpendingViewModel(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    initialDate: LocalDate
) : ViewModel() {

    private val selectedDate = MutableStateFlow(initialDate)

    val uiState: StateFlow<DailySpendingUiState> =
        combine(
            transactionRepository.getTransactions(),
            categoryRepository.getCategories(),
            selectedDate
        ) { transactions, categories, selected ->
            transactions.toUiState(categories, selected)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DailySpendingUiState(selectedDate = initialDate)
        )

    fun onDaySelected(date: LocalDate) {
        selectedDate.value = date
    }

    private fun List<Transaction>.toUiState(
        categories: List<Category>,
        selected: LocalDate
    ): DailySpendingUiState {
        // The 30-day window always ends on the real "today", regardless of
        // which day within it is selected -- selecting an older day changes
        // what's highlighted and listed below, not the chart's span.
        val today = LocalDate.now()
        val periodStart = today.minusDays((DAILY_SPENDING_PERIOD_DAYS - 1).toLong())

        val expensesByDay = filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.transactionDate.toLocalDate() }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        val periodBars = (0 until DAILY_SPENDING_PERIOD_DAYS).map { offset ->
            val date = periodStart.plusDays(offset.toLong())
            DailyBar(date, expensesByDay[date] ?: 0.0)
        }

        val periodAverage = periodBars.sumOf { it.amount } / periodBars.size
        val selectedDayTotal = expensesByDay[selected] ?: 0.0
        val comparisonPercent = if (periodAverage > 0.0) {
            (((selectedDayTotal - periodAverage) / periodAverage) * 100).roundToInt()
        } else {
            null
        }

        val selectedDayTransactions = filter {
            it.type == TransactionType.EXPENSE && it.transactionDate.toLocalDate() == selected
        }.sortedByDescending { it.transactionDate }

        return DailySpendingUiState(
            selectedDate = selected,
            periodBars = periodBars,
            periodAverage = periodAverage,
            selectedDayTotal = selectedDayTotal,
            comparisonPercent = comparisonPercent,
            selectedDayTransactions = selectedDayTransactions,
            categoriesById = categories.associateBy { it.id },
            isLoading = false
        )
    }

    class Factory(
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository,
        private val initialDate: LocalDate
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DailySpendingViewModel(transactionRepository, categoryRepository, initialDate) as T
    }
}
