package com.pesatrack.app.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pesatrack.app.di.AppModule
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.navigation.Screen
import com.pesatrack.app.ui.theme.Background
import com.pesatrack.app.ui.theme.Divider
import com.pesatrack.app.ui.theme.PrimaryDark
import com.pesatrack.app.ui.theme.Surface
import com.pesatrack.app.ui.theme.components.EmptyState
import com.pesatrack.app.ui.theme.components.MoneyCard
import com.pesatrack.app.ui.theme.components.MonthSelector
import com.pesatrack.app.ui.theme.components.PesaBottomBar
import com.pesatrack.app.ui.theme.components.TransactionRow

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { AppModule.provideTransactionRepository(context) }
    val categoryRepository = remember { AppModule.provideCategoryRepository(context) }
    val budgetRepository = remember { AppModule.provideBudgetRepository(context) }
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(repository, categoryRepository, budgetRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = { PesaBottomBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryDark)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Text(
                        text = "Hello, Owen",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    MonthSelector(
                        month = uiState.month,
                        onMonthSelected = viewModel::onMonthSelected,
                        contentColor = Color.White.copy(alpha = 0.82f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MoneyCard(
                    todaySpending = uiState.todaySpending,
                    monthIncome = uiState.monthIncome,
                    remainingBudget = uiState.remainingBudget,
                    onRemainingBudgetClick = { navController.navigate(Screen.Budgets.route) }
                )

                Text(
                    text = "Recent transactions",
                    style = MaterialTheme.typography.titleMedium
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        if (uiState.recentTransactions.isEmpty()) {
                            EmptyState(
                                icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                                text = "No transactions yet"
                            )
                        } else {
                            uiState.recentTransactions.forEachIndexed { index, transaction ->
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
}
