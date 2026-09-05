package com.pesatrack.app.presentation.reports

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pesatrack.app.di.AppModule
import com.pesatrack.app.ui.theme.Background
import com.pesatrack.app.ui.theme.Divider
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.PrimaryDark
import com.pesatrack.app.ui.theme.StatusBarIcons
import com.pesatrack.app.ui.theme.Surface
import com.pesatrack.app.ui.theme.TextPrimary
import com.pesatrack.app.ui.theme.TextSecondary
import com.pesatrack.app.ui.theme.components.EmptyState
import com.pesatrack.app.ui.theme.components.MonthSelector
import com.pesatrack.app.ui.theme.components.PesaBottomBar
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val monthFormat = DateTimeFormatter.ofPattern("MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {
    val context = LocalContext.current
    val transactionRepository = remember { AppModule.provideTransactionRepository(context) }
    val categoryRepository = remember { AppModule.provideCategoryRepository(context) }
    val viewModel: ReportsViewModel = viewModel(
        factory = ReportsViewModel.Factory(context, transactionRepository, categoryRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let(viewModel::exportCsv) }

    StatusBarIcons(darkIcons = false)

    Scaffold(
        bottomBar = { PesaBottomBar(navController) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Reports") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { exportLauncher.launch("pesatrack-report-${uiState.month}.csv") },
                            enabled = !uiState.isExporting
                        ) {
                            Icon(Icons.Outlined.IosShare, contentDescription = "Export")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryDark,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
                MonthSelector(
                    month = uiState.month,
                    onMonthSelected = viewModel::onMonthSelected,
                    formatter = monthFormat,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryDark)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
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
                        text = "Expenses overview",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    if (uiState.totalExpense <= 0.0) {
                        EmptyState(
                            icon = Icons.Outlined.PieChart,
                            text = "No expenses this month"
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DonutChart(
                                slices = uiState.categorySlices,
                                total = uiState.totalExpense
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                uiState.categorySlices.forEach { slice ->
                                    LegendRow(slice)
                                }
                            }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily trend",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = uiState.month.format(monthFormat),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    if (uiState.totalExpense <= 0.0) {
                        EmptyState(
                            icon = Icons.AutoMirrored.Outlined.ShowChart,
                            text = "No expenses this month"
                        )
                    } else {
                        Row(modifier = Modifier.padding(top = 8.dp)) {
                            TrendAxisLabels(uiState.trendMax)
                            DailyTrendChart(
                                points = uiState.dailyTrend,
                                maxValue = uiState.trendMax,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(90.dp)
                            )
                        }
                        DayLabels(dayCount = uiState.dailyTrend.size, month = uiState.month)
                    }
                }
            }

            if (uiState.isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(24.dp),
                    color = Primary,
                    strokeWidth = 2.dp
                )
            }
        }
    }

    uiState.exportMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissExportMessage,
            title = { Text("Export") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissExportMessage) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun DonutChart(
    slices: List<CategorySlice>,
    total: Double,
    modifier: Modifier = Modifier
) {
    val dividerColor = Divider
    val holeColor = Surface

    Box(
        modifier = modifier.size(86.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (slices.isEmpty() || total <= 0.0) {
                drawCircle(color = dividerColor)
            } else {
                var startAngle = -90f
                slices.forEach { slice ->
                    val sweep = (slice.amount / total * 360f).toFloat()
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true
                    )
                    startAngle += sweep
                }
            }
            drawCircle(
                color = holeColor,
                radius = size.minDimension / 2f * (56f / 86f)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "KSh",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = formatCompactNumber(total),
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary
            )
            Text(
                text = "total",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun LegendRow(slice: CategorySlice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(slice.color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = slice.label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${slice.percent}%",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary
        )
    }
}

@Composable
private fun TrendAxisLabels(maxValue: Double) {
    Column(
        modifier = Modifier
            .width(28.dp)
            .height(90.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(formatCompactNumber(maxValue), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(Modifier.weight(1f))
        Text(formatCompactNumber(maxValue / 2), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(Modifier.weight(1f))
        Text("0", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun DailyTrendChart(
    points: List<DailyPoint>,
    maxValue: Double,
    modifier: Modifier = Modifier
) {
    val gridColor = Divider
    val dotHoleColor = Surface

    Canvas(modifier = modifier) {
        listOf(0f, 0.5f, 1f).forEach { fraction ->
            val y = size.height * fraction
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (points.isNotEmpty() && maxValue > 0.0) {
            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
            val path = Path()
            points.forEachIndexed { index, point ->
                val x = stepX * index
                val y = size.height * (1 - (point.amount / maxValue).toFloat().coerceIn(0f, 1f))
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = Primary,
                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            val lastIndex = points.size - 1
            val lastX = stepX * lastIndex
            val lastY = size.height * (1 - (points[lastIndex].amount / maxValue).toFloat().coerceIn(0f, 1f))
            drawCircle(color = Primary, radius = 4.dp.toPx(), center = Offset(lastX, lastY))
            drawCircle(color = dotHoleColor, radius = 2.dp.toPx(), center = Offset(lastX, lastY))
        }
    }
}

@Composable
private fun DayLabels(dayCount: Int, month: YearMonth) {
    if (dayCount == 0) return

    val labelDays = (0..4).map { index -> 1 + (index * (dayCount - 1) / 4) }
    val monthAbbreviation = month.format(DateTimeFormatter.ofPattern("MMM"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 28.dp, top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labelDays.forEach { day ->
            Text(
                text = "${day} ${monthAbbreviation}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

private fun formatCompactNumber(amount: Double): String {
    if (amount < 1000) return amount.toInt().toString()
    val thousands = amount / 1000
    val rounded = (thousands * 10).toInt() / 10.0
    return if (rounded == rounded.toInt().toDouble()) "${rounded.toInt()}K" else "${rounded}K"
}
