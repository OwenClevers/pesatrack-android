package com.pesatrack.app.presentation.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pesatrack.app.core.formatKsh
import com.pesatrack.app.di.AppModule
import com.pesatrack.app.ui.theme.Accent
import com.pesatrack.app.ui.theme.Background
import com.pesatrack.app.ui.theme.Expense
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.PrimaryDark
import com.pesatrack.app.ui.theme.Surface
import com.pesatrack.app.ui.theme.TextPrimary
import com.pesatrack.app.ui.theme.TextSecondary
import com.pesatrack.app.ui.theme.components.EmptyState
import com.pesatrack.app.ui.theme.components.PesaBottomBar
import com.pesatrack.app.ui.theme.components.visual
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val TrackBackground = Color(0xFFEDEFF3)
private val AccentText = Color(0xFFA67C00)
private val monthFormat = DateTimeFormatter.ofPattern("MMMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(navController: NavController) {
    val context = LocalContext.current
    val budgetRepository = remember { AppModule.provideBudgetRepository(context) }
    val transactionRepository = remember { AppModule.provideTransactionRepository(context) }
    val categoryRepository = remember { AppModule.provideCategoryRepository(context) }
    val viewModel: BudgetsViewModel = viewModel(
        factory = BudgetsViewModel.Factory(budgetRepository, transactionRepository, categoryRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = { PesaBottomBar(navController) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Budgets") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.AddCircle, contentDescription = "Add budget")
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
                    onMonthSelected = viewModel::onMonthSelected
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.rows.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.AddCircle,
                    text = "No budgets yet"
                )
            } else {
                uiState.rows.forEach { row ->
                    BudgetCard(row)
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    month: YearMonth,
    onMonthSelected: (YearMonth) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val months = remember { (0..11).map { YearMonth.now().minusMonths(it.toLong()) } }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryDark)
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = month.format(monthFormat),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.88f)
            )
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.88f),
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            months.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.format(monthFormat)) },
                    onClick = {
                        onMonthSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BudgetCard(row: BudgetRow) {
    val visual = row.category.visual()
    val health = if (row.percent >= 90) Expense else if (row.percent >= 60) Accent else Primary
    val healthText = if (row.percent >= 90) Expense else if (row.percent >= 60) AccentText else Primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(visual.container),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = null,
                        tint = visual.content,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = row.category.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "${formatKsh(row.spent)} of ${formatKsh(row.limit)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "${row.percent}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = healthText
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(TrackBackground)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (row.percent / 100f).coerceIn(0f, 1f))
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(health)
                )
            }
        }
    }
}
