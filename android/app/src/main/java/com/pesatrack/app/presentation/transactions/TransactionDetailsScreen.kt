package com.pesatrack.app.presentation.transactions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pesatrack.app.core.formatKsh
import com.pesatrack.app.di.AppModule
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.navigation.Screen
import com.pesatrack.app.ui.theme.Background
import com.pesatrack.app.ui.theme.Divider
import com.pesatrack.app.ui.theme.Expense
import com.pesatrack.app.ui.theme.FuelContainerDark
import com.pesatrack.app.ui.theme.FuelContentDark
import com.pesatrack.app.ui.theme.Income
import com.pesatrack.app.ui.theme.LocalPesaTrackColors
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.PrimaryDark
import com.pesatrack.app.ui.theme.Surface
import com.pesatrack.app.ui.theme.TextPrimary
import com.pesatrack.app.ui.theme.TextSecondary
import com.pesatrack.app.ui.theme.components.visual
import java.time.format.DateTimeFormatter

private val dateTimeFormat = DateTimeFormatter.ofPattern("d MMM yyyy · hh:mm a")
private val pillContainer = Color(0xFFE8F5E9)
private val pillContent = Color(0xFF1B5E20)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(navController: NavController, transactionId: Long) {
    val context = LocalContext.current
    val transactionRepository = remember { AppModule.provideTransactionRepository(context) }
    val categoryRepository = remember { AppModule.provideCategoryRepository(context) }
    val viewModel: TransactionDetailsViewModel = viewModel(
        factory = TransactionDetailsViewModel.Factory(transactionId, transactionRepository, categoryRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.transaction, uiState.isLoading) {
        if (!uiState.isLoading && uiState.transaction == null) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Transaction details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        val transaction = uiState.transaction
        val category = uiState.category

        if (transaction != null && category != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TransactionSummary(transaction, category)

                        HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 12.dp))

                        DetailRow(
                            label = "Category",
                            value = category.name,
                            icon = category.visual().icon,
                            iconTint = category.visual().content
                        )
                        HorizontalDivider(color = Divider)
                        DetailRow(
                            label = "Date",
                            value = transaction.transactionDate.format(dateTimeFormat)
                        )
                        HorizontalDivider(color = Divider)
                        DetailRow(
                            label = "Payment method",
                            value = "Cash",
                            icon = Icons.Outlined.Payments,
                            iconTint = TextSecondary
                        )
                        transaction.description?.let { description ->
                            HorizontalDivider(color = Divider)
                            DetailRow(label = "Description", value = description)
                        }
                        HorizontalDivider(color = Divider)
                        SourceRow(transaction.source)
                    }
                }

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.EditTransaction.route(transaction.id)) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        border = BorderStroke(1.dp, Primary)
                    ) {
                        Text("Edit")
                    }
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Expense),
                        border = BorderStroke(1.dp, Expense)
                    ) {
                        Text("Delete")
                    }
                }
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete transaction?") },
                text = { Text("This can't be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                        viewModel.delete()
                    }) {
                        Text("Delete", color = Expense)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun TransactionSummary(transaction: Transaction, category: Category) {
    val isIncome = transaction.type == TransactionType.INCOME
    val visual = category.visual()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(visual.container),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = visual.icon,
                contentDescription = null,
                tint = visual.content,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = if (isIncome) "Income" else "Expense",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = transaction.merchant ?: category.name,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Text(
            text = (if (isIncome) "+ " else "- ") + formatKsh(transaction.amount),
            style = MaterialTheme.typography.headlineMedium,
            color = if (isIncome) Income else Expense
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: ImageVector? = null,
    iconTint: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun SourceRow(source: TransactionSource) {
    val isDark = LocalPesaTrackColors.current.isDark

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Source",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isDark) FuelContainerDark else pillContainer)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = source.displayLabel(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) FuelContentDark else pillContent
            )
        }
    }
}

private fun TransactionSource.displayLabel(): String = when (this) {
    TransactionSource.MANUAL -> "Manually added"
    TransactionSource.MPESA_SMS -> "M-Pesa SMS"
    TransactionSource.IMPORT -> "Imported"
    TransactionSource.BANK_SYNC -> "Bank sync"
}
