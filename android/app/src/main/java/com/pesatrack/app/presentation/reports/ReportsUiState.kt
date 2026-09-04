package com.pesatrack.app.presentation.reports

import androidx.compose.ui.graphics.Color
import java.time.YearMonth

data class ReportsUiState(
    val month: YearMonth = YearMonth.now(),
    val totalExpense: Double = 0.0,
    val categorySlices: List<CategorySlice> = emptyList(),
    val dailyTrend: List<DailyPoint> = emptyList(),
    val trendMax: Double = 0.0,
    val isLoading: Boolean = true
)

data class CategorySlice(
    val label: String,
    val amount: Double,
    val percent: Int,
    val color: Color
)

data class DailyPoint(
    val day: Int,
    val amount: Double
)
