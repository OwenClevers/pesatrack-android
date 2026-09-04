package com.pesatrack.app.presentation.reports

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.domain.repository.CategoryRepository
import com.pesatrack.app.domain.repository.TransactionRepository
import com.pesatrack.app.ui.theme.Accent
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.PrimaryDark
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.roundToInt

private val RankColors = listOf(PrimaryDark, Primary, Accent, Color(0xFF1565C0))
private val OtherColor = Color(0xFFB6BCC6)

class ReportsViewModel(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<ReportsUiState> =
        combine(
            transactionRepository.getTransactions(),
            categoryRepository.getCategories()
        ) { transactions, categories -> buildState(transactions, categories) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ReportsUiState()
            )

    private fun buildState(transactions: List<Transaction>, categories: List<Category>): ReportsUiState {
        val thisMonth = YearMonth.now()
        val categoriesById = categories.associateBy { it.id }

        val monthExpenses = transactions.filter {
            it.type == TransactionType.EXPENSE && YearMonth.from(it.transactionDate) == thisMonth
        }

        val totalExpense = monthExpenses.sumOf { it.amount }

        val byCategory = monthExpenses
            .groupBy { it.categoryId }
            .map { (categoryId, txns) -> categoryId to txns.sumOf { it.amount } }
            .sortedByDescending { it.second }

        val topSlices = byCategory.take(4).mapIndexed { index, (categoryId, amount) ->
            CategorySlice(
                label = categoriesById[categoryId]?.name ?: "Other",
                amount = amount,
                percent = percentOf(amount, totalExpense),
                color = RankColors[index]
            )
        }
        val otherAmount = byCategory.drop(4).sumOf { it.second }
        val categorySlices = if (otherAmount > 0) {
            topSlices + CategorySlice("Other", otherAmount, percentOf(otherAmount, totalExpense), OtherColor)
        } else {
            topSlices
        }

        val daysInMonth = thisMonth.lengthOfMonth()
        val dailyTrend = (1..daysInMonth).map { day ->
            val amount = monthExpenses
                .filter { it.transactionDate.dayOfMonth == day }
                .sumOf { it.amount }
            DailyPoint(day, amount)
        }
        val trendMax = niceMax(dailyTrend.maxOfOrNull { it.amount } ?: 0.0)

        return ReportsUiState(
            totalExpense = totalExpense,
            categorySlices = categorySlices,
            dailyTrend = dailyTrend,
            trendMax = trendMax,
            isLoading = false
        )
    }

    private fun percentOf(amount: Double, total: Double): Int =
        if (total > 0) (amount / total * 100).roundToInt() else 0

    // Rounds up to a "nice" axis max (1/2/5/10 x a power of ten) so gridline labels read cleanly.
    private fun niceMax(value: Double): Double {
        if (value <= 0.0) return 1000.0
        val magnitude = Math.pow(10.0, floor(log10(value)))
        val normalized = value / magnitude
        val niceNormalized = when {
            normalized <= 1 -> 1.0
            normalized <= 2 -> 2.0
            normalized <= 5 -> 5.0
            else -> 10.0
        }
        return niceNormalized * magnitude
    }

    class Factory(
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ReportsViewModel(transactionRepository, categoryRepository) as T
    }
}
