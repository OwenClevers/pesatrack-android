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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pesatrack.app.core.formatKsh
import com.pesatrack.app.di.AppModule
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.ui.theme.Accent
import com.pesatrack.app.ui.theme.Background
import com.pesatrack.app.ui.theme.Expense
import com.pesatrack.app.ui.theme.FoodContentDark
import com.pesatrack.app.ui.theme.LocalPesaTrackColors
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.PrimaryDark
import com.pesatrack.app.ui.theme.Surface
import com.pesatrack.app.ui.theme.TextPrimary
import com.pesatrack.app.ui.theme.TextSecondary
import com.pesatrack.app.ui.theme.components.EmptyState
import com.pesatrack.app.ui.theme.components.MonthSelector
import com.pesatrack.app.ui.theme.components.PesaBottomBar
import com.pesatrack.app.ui.theme.components.visual

private val TrackBackground = Color(0xFFEDEFF3)
private val TrackBackgroundDark = Color(0xFF3A3D42)
private val AccentText = Color(0xFFA67C00)

// id == 0 means "creating a new budget"; a real id means editing that row in place.
private data class BudgetSheetTarget(
    val id: Long,
    val category: Category?,
    val limitText: String
)

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

    var sheetTarget by remember { mutableStateOf<BudgetSheetTarget?>(null) }

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
                        IconButton(onClick = {
                            sheetTarget = BudgetSheetTarget(id = 0, category = null, limitText = "")
                        }) {
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
                    onMonthSelected = viewModel::onMonthSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryDark)
                        .padding(horizontal = 20.dp, vertical = 4.dp)
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
                    BudgetCard(
                        row = row,
                        onClick = {
                            sheetTarget = BudgetSheetTarget(
                                id = row.budgetId,
                                category = row.category,
                                limitText = formatLimitText(row.limit)
                            )
                        }
                    )
                }
            }
        }

        sheetTarget?.let { target ->
            BudgetSheet(
                target = target,
                // A category already budgeted this month can't be picked again for a
                // *new* budget -- saving would silently overwrite that other row.
                // Editing keeps its own category available even though it's "in use".
                availableCategories = uiState.categories.filter { category ->
                    target.id != 0L || uiState.rows.none { it.category.id == category.id }
                },
                onDismiss = { sheetTarget = null },
                onSave = { categoryId, limit ->
                    viewModel.saveBudget(budgetId = target.id, categoryId = categoryId, limit = limit)
                    sheetTarget = null
                }
            )
        }
    }
}

private fun formatLimitText(limit: Double): String =
    if (limit == limit.toLong().toDouble()) limit.toLong().toString() else limit.toString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetSheet(
    target: BudgetSheetTarget,
    availableCategories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (categoryId: Long, limit: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var category by remember(target) { mutableStateOf(target.category) }
    var limitText by remember(target) { mutableStateOf(target.limitText) }
    val limit = limitText.toDoubleOrNull()
    val canSave = category != null && limit != null && limit > 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (target.id == 0L) "New budget" else "Edit budget",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            BudgetCategoryField(
                selected = category,
                categories = availableCategories,
                enabled = target.id == 0L,
                onSelect = { category = it }
            )

            OutlinedTextField(
                value = limitText,
                onValueChange = { limitText = it },
                label = { Text("Monthly limit") },
                placeholder = { Text("KSh 0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onSave(category!!.id, limit!!) },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Save")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetCategoryField(
    selected: Category?,
    categories: List<Category>,
    enabled: Boolean,
    onSelect: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            label = { Text("Category") },
            placeholder = { Text("Select category") },
            trailingIcon = if (enabled) {
                { Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null) }
            } else null,
            leadingIcon = selected?.let { category ->
                {
                    Icon(
                        imageVector = category.visual().icon,
                        contentDescription = null,
                        tint = category.visual().content
                    )
                }
            }
        )

        if (enabled) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = category.visual().icon,
                                contentDescription = null,
                                tint = category.visual().content
                            )
                        },
                        onClick = {
                            onSelect(category)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun BudgetCard(row: BudgetRow, onClick: () -> Unit) {
    val isDark = LocalPesaTrackColors.current.isDark
    val visual = row.category.visual()
    val health = if (row.percent >= 90) Expense else if (row.percent >= 60) Accent else Primary
    val healthText = if (row.percent >= 90) {
        Expense
    } else if (row.percent >= 60) {
        if (isDark) FoodContentDark else AccentText
    } else {
        Primary
    }

    Card(
        onClick = onClick,
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
                    .background(if (isDark) TrackBackgroundDark else TrackBackground)
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
