package com.pesatrack.app.presentation.transactions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Category as CategoryIcon
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pesatrack.app.di.AppModule
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.navigation.Screen
import com.pesatrack.app.ui.theme.Background
import com.pesatrack.app.ui.theme.Divider
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.PrimaryDark
import com.pesatrack.app.ui.theme.StatusBarIcons
import com.pesatrack.app.ui.theme.Surface
import com.pesatrack.app.ui.theme.TextPrimary
import com.pesatrack.app.ui.theme.TextSecondary
import com.pesatrack.app.ui.theme.components.EmptyState
import com.pesatrack.app.ui.theme.components.PesaBottomBar
import com.pesatrack.app.ui.theme.components.TransactionRow
import com.pesatrack.app.ui.theme.components.visual
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val filterDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy")

@Composable
fun TransactionsScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { AppModule.provideTransactionRepository(context) }
    val categoryRepository = remember { AppModule.provideCategoryRepository(context) }
    val merchantCategoryRepository = remember { AppModule.provideMerchantCategoryRepository(context) }
    val viewModel: TransactionsViewModel = viewModel(
        factory = TransactionsViewModel.Factory(repository, categoryRepository, merchantCategoryRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showBulkCategorySheet by remember { mutableStateOf(false) }
    val selectionMode = uiState.selectedIds.isNotEmpty()

    StatusBarIcons(darkIcons = false)

    Scaffold(
        // No topBar here either -- see the identical comment in DashboardScreen.
        containerColor = PrimaryDark,
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
            ) {
                if (selectionMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Cancel selection",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "${uiState.selectedIds.size} selected",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showBulkCategorySheet = true }) {
                            Icon(
                                imageVector = Icons.Outlined.CategoryIcon,
                                contentDescription = "Assign category",
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Transactions",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text("Search transactions", color = Color.White.copy(alpha = 0.7f))
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.White)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.16f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.16f),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White,
                                focusedLeadingIconColor = Color.White,
                                unfocusedLeadingIconColor = Color.White
                            )
                        )
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                imageVector = Icons.Outlined.FilterList,
                                contentDescription = "Filter",
                                tint = if (uiState.filter.isActive) Color.White else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            if (uiState.groups.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Search,
                    text = if (uiState.searchQuery.isNotBlank() || uiState.filter.isActive) {
                        "No matching transactions"
                    } else {
                        "No transactions yet"
                    },
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // LazyColumn, one item per day group -- a real M-Pesa import
                // history can run into the thousands of transactions, and a
                // plain Column composing all of them (and every group) at
                // once OOMs. A day's worth of transactions is naturally
                // bounded, so lazily loading at group granularity (rather
                // than per-row) is enough while keeping each day's card as a
                // single visual/composable unit, unchanged from before.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.groups, key = { it.label }) { group ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = group.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Surface),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    group.transactions.forEachIndexed { index, transaction ->
                                        if (index > 0) {
                                            HorizontalDivider(color = Divider)
                                        }
                                        TransactionRow(
                                            transaction = transaction,
                                            category = uiState.categoriesById[transaction.categoryId]
                                                ?: Category.unknown(transaction.categoryId),
                                            onClick = {
                                                if (selectionMode) {
                                                    viewModel.onToggleSelected(transaction.id)
                                                } else {
                                                    navController.navigate(Screen.TransactionDetails.route(transaction.id))
                                                }
                                            },
                                            onLongClick = { viewModel.onToggleSelected(transaction.id) },
                                            selectionMode = selectionMode,
                                            selected = transaction.id in uiState.selectedIds
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterSheet(
            initialFilter = uiState.filter,
            categories = uiState.categories,
            onDismiss = { showFilterSheet = false },
            onApply = { criteria ->
                viewModel.onFilterChange(criteria)
                showFilterSheet = false
            }
        )
    }

    if (showBulkCategorySheet) {
        BulkCategorySheet(
            categories = uiState.categories,
            onDismiss = { showBulkCategorySheet = false },
            onSelect = { categoryId ->
                viewModel.assignCategoryToSelected(categoryId)
                showBulkCategorySheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BulkCategorySheet(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

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
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Assign category",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            categories.forEach { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelect(category.id) }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = category.visual().icon,
                        contentDescription = null,
                        tint = category.visual().content
                    )
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    initialFilter: TransactionFilter,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onApply: (TransactionFilter) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var type by remember { mutableStateOf(initialFilter.type) }
    var categoryId by remember { mutableStateOf(initialFilter.categoryId) }
    var startDate by remember { mutableStateOf(initialFilter.startDate) }
    var endDate by remember { mutableStateOf(initialFilter.endDate) }

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
                text = "Filter transactions",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Type", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeFilterOption(
                        label = "All",
                        selected = type == null,
                        modifier = Modifier.weight(1f),
                        onClick = { type = null }
                    )
                    TypeFilterOption(
                        label = "Expense",
                        selected = type == TransactionType.EXPENSE,
                        modifier = Modifier.weight(1f),
                        onClick = { type = TransactionType.EXPENSE }
                    )
                    TypeFilterOption(
                        label = "Income",
                        selected = type == TransactionType.INCOME,
                        modifier = Modifier.weight(1f),
                        onClick = { type = TransactionType.INCOME }
                    )
                }
            }

            CategoryFilterField(
                categories = categories,
                selectedId = categoryId,
                onSelect = { categoryId = it }
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Date range", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateFilterField(
                        label = "Start date",
                        date = startDate,
                        modifier = Modifier.weight(1f),
                        onDateChange = { startDate = it },
                        onClear = { startDate = null }
                    )
                    DateFilterField(
                        label = "End date",
                        date = endDate,
                        modifier = Modifier.weight(1f),
                        onDateChange = { endDate = it },
                        onClear = { endDate = null }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        type = null
                        categoryId = null
                        startDate = null
                        endDate = null
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = BorderStroke(1.dp, Divider)
                ) {
                    Text("Reset")
                }

                Button(
                    onClick = { onApply(TransactionFilter(type, categoryId, startDate, endDate)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun TypeFilterOption(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val container = if (selected) Primary else Surface
    val content = if (selected) Color.White else TextSecondary
    val border = if (selected) Primary else Divider

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .border(1.dp, border, RoundedCornerShape(9.dp))
            .background(container, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterField(
    categories: List<Category>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.firstOrNull { it.id == selectedId }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Category", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "All categories",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                trailingIcon = {
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                },
                leadingIcon = selectedCategory?.let { category ->
                    {
                        Icon(
                            imageVector = category.visual().icon,
                            contentDescription = null,
                            tint = category.visual().content
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All categories") },
                    onClick = {
                        onSelect(null)
                        expanded = false
                    }
                )
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
                            onSelect(category.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFilterField(
    label: String,
    date: LocalDate?,
    modifier: Modifier = Modifier,
    onDateChange: (LocalDate) -> Unit,
    onClear: () -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Divider, RoundedCornerShape(10.dp))
                .clickable { showPicker = true }
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date?.format(filterDateFormat) ?: "Any",
                style = MaterialTheme.typography.bodyMedium,
                color = if (date != null) TextPrimary else TextSecondary
            )
            if (date != null) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Clear $label",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onClear)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (date ?: LocalDate.now())
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateChange(
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                        )
                    }
                    showPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
