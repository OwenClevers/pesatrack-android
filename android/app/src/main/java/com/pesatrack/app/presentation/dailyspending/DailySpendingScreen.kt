package com.pesatrack.app.presentation.dailyspending

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pesatrack.app.core.formatKsh
import com.pesatrack.app.di.AppModule
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.navigation.Screen
import com.pesatrack.app.ui.theme.Background
import com.pesatrack.app.ui.theme.Divider
import com.pesatrack.app.ui.theme.Expense
import com.pesatrack.app.ui.theme.Income
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.PrimaryDark
import com.pesatrack.app.ui.theme.StatusBarIcons
import com.pesatrack.app.ui.theme.Surface
import com.pesatrack.app.ui.theme.TextPrimary
import com.pesatrack.app.ui.theme.TextSecondary
import com.pesatrack.app.ui.theme.components.EmptyState
import com.pesatrack.app.ui.theme.components.TransactionRow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dayFormat = DateTimeFormatter.ofPattern("d MMM")
private val fullDayFormat = DateTimeFormatter.ofPattern("EEEE, d MMM")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySpendingScreen(navController: NavController, initialDate: LocalDate) {
    val context = LocalContext.current
    val transactionRepository = remember { AppModule.provideTransactionRepository(context) }
    val categoryRepository = remember { AppModule.provideCategoryRepository(context) }
    val viewModel: DailySpendingViewModel = viewModel(
        factory = DailySpendingViewModel.Factory(transactionRepository, categoryRepository, initialDate)
    )
    val uiState by viewModel.uiState.collectAsState()

    StatusBarIcons(darkIcons = false)

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Daily spending") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Last $DAILY_SPENDING_PERIOD_DAYS days",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    if (uiState.periodBars.all { it.amount <= 0.0 }) {
                        EmptyState(
                            icon = Icons.AutoMirrored.Outlined.ShowChart,
                            text = "No expenses in this period"
                        )
                    } else {
                        PeriodBarChart(
                            bars = uiState.periodBars,
                            selectedDate = uiState.selectedDate,
                            onBarClick = viewModel::onDaySelected,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(top = 8.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.periodBars.first().date.format(dayFormat),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = uiState.periodBars.last().date.format(dayFormat),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = uiState.selectedDate.format(fullDayFormat),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = formatKsh(uiState.selectedDayTotal),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    ComparisonLine(
                        comparisonPercent = uiState.comparisonPercent,
                        periodAverage = uiState.periodAverage
                    )
                }
            }

            Text(
                text = "Transactions",
                style = MaterialTheme.typography.titleMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (uiState.selectedDayTransactions.isEmpty()) {
                        EmptyState(
                            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                            text = "No expenses on this day"
                        )
                    } else {
                        uiState.selectedDayTransactions.forEachIndexed { index, transaction ->
                            if (index > 0) {
                                HorizontalDivider(color = Divider)
                            }
                            TransactionRow(
                                transaction = transaction,
                                category = uiState.categoriesById[transaction.categoryId]
                                    ?: Category.unknown(transaction.categoryId),
                                onClick = {
                                    navController.navigate(Screen.TransactionDetails.route(transaction.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonLine(comparisonPercent: Int?, periodAverage: Double) {
    val averageText = formatKsh(periodAverage)
    val (text, color) = when {
        comparisonPercent == null ->
            "No spending history yet to compare against." to TextSecondary
        comparisonPercent > 0 ->
            "$comparisonPercent% above your $DAILY_SPENDING_PERIOD_DAYS-day average of $averageText" to Expense
        comparisonPercent < 0 ->
            "${-comparisonPercent}% below your $DAILY_SPENDING_PERIOD_DAYS-day average of $averageText" to Income
        else ->
            "Right at your $DAILY_SPENDING_PERIOD_DAYS-day average of $averageText" to TextSecondary
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = Modifier.padding(top = 4.dp)
    )
}

// Tapping anywhere along a bar's column selects that bar's day, however
// short the bar is -- every day in the period, including zero-spend ones,
// gets a visible, tappable sliver (see barHeight below) so sparse data
// never leaves a gap a user can't select.
@Composable
private fun PeriodBarChart(
    bars: List<DailyBar>,
    selectedDate: LocalDate,
    onBarClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val barColor = Primary
    val baselineColor = Divider

    Canvas(
        modifier = modifier.pointerInput(bars) {
            detectTapGestures { offset ->
                if (bars.isEmpty()) return@detectTapGestures
                val barWidth = size.width / bars.size
                val index = (offset.x / barWidth).toInt().coerceIn(0, bars.size - 1)
                onBarClick(bars[index].date)
            }
        }
    ) {
        drawLine(
            color = baselineColor,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
        )

        if (bars.isEmpty()) return@Canvas

        val maxAmount = bars.maxOf { it.amount }.coerceAtLeast(1.0)
        val barWidth = size.width / bars.size
        val gap = (barWidth * 0.25f).coerceAtMost(3.dp.toPx())

        bars.forEachIndexed { index, bar ->
            val isSelected = bar.date == selectedDate
            val heightFraction = (bar.amount / maxAmount).toFloat().coerceIn(0f, 1f)
            val barHeight = size.height * heightFraction.coerceAtLeast(0.04f)
            val left = index * barWidth + gap / 2
            drawRoundRect(
                color = if (isSelected) barColor else barColor.copy(alpha = 0.35f),
                topLeft = Offset(left, size.height - barHeight),
                size = Size((barWidth - gap).coerceAtLeast(1f), barHeight),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
        }
    }
}
