package com.pesatrack.app.presentation.dailyspending

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import java.time.LocalDate

data class DailySpendingUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val periodBars: List<DailyBar> = emptyList(),
    val periodAverage: Double = 0.0,
    val selectedDayTotal: Double = 0.0,
    // Percent selectedDayTotal is above (positive) or below (negative) the
    // period average. Null when the average is zero -- there's nothing
    // meaningful to compare against.
    val comparisonPercent: Int? = null,
    val selectedDayTransactions: List<Transaction> = emptyList(),
    val categoriesById: Map<Long, Category> = emptyMap(),
    val isLoading: Boolean = true
)

// One bar of the 30-day chart.
data class DailyBar(
    val date: LocalDate,
    val amount: Double
)
