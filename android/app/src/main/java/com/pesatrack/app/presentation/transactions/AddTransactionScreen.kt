package com.pesatrack.app.presentation.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pesatrack.app.di.AppModule
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.ui.theme.Divider
import com.pesatrack.app.ui.theme.Expense
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.PrimaryDark
import com.pesatrack.app.ui.theme.Surface
import com.pesatrack.app.ui.theme.TextPrimary
import com.pesatrack.app.ui.theme.TextSecondary
import com.pesatrack.app.ui.theme.components.visual
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { AppModule.provideTransactionRepository(context) }
    val categoryRepository = remember { AppModule.provideCategoryRepository(context) }
    val viewModel: AddTransactionViewModel = viewModel(
        factory = AddTransactionViewModel.Factory(repository, categoryRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveComplete) {
        if (uiState.saveComplete) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Add transaction") },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FormField(label = "Amount") {
                OutlinedTextField(
                    value = uiState.amountText,
                    onValueChange = viewModel::onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("KSh 0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            FormField(label = "Type") {
                TypeSegmentedControl(
                    selected = uiState.type,
                    onSelect = viewModel::onTypeChange
                )
            }

            FormField(label = "Category") {
                CategoryDropdown(
                    selected = uiState.category,
                    categories = uiState.categories,
                    onSelect = viewModel::onCategoryChange
                )
            }

            FormField(label = "Merchant") {
                OutlinedTextField(
                    value = uiState.merchant,
                    onValueChange = viewModel::onMerchantChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            FormField(label = "Description (optional)") {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            FormField(label = "Date") {
                DateField(
                    date = uiState.date,
                    onDateChange = viewModel::onDateChange
                )
            }

            Button(
                onClick = viewModel::save,
                enabled = uiState.canSave,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Save transaction")
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        content()
    }
}

@Composable
private fun TypeSegmentedControl(
    selected: TransactionType,
    onSelect: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SegmentedOption(
            label = "Expense",
            selected = selected == TransactionType.EXPENSE,
            selectedContainer = Expense,
            selectedContent = Color.White,
            selectedBorder = Expense,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(TransactionType.EXPENSE) }
        )
        SegmentedOption(
            label = "Income",
            selected = selected == TransactionType.INCOME,
            selectedContainer = Surface,
            selectedContent = Primary,
            selectedBorder = Primary,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(TransactionType.INCOME) }
        )
    }
}

@Composable
private fun SegmentedOption(
    label: String,
    selected: Boolean,
    selectedContainer: Color,
    selectedContent: Color,
    selectedBorder: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val container = if (selected) selectedContainer else Surface
    val content = if (selected) selectedContent else TextSecondary
    val border = if (selected) selectedBorder else Divider

    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(1.dp, border, RoundedCornerShape(9.dp))
            .background(container, RoundedCornerShape(9.dp))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selected: Category?,
    categories: List<Category>,
    onSelect: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            placeholder = { Text("Select category") },
            trailingIcon = {
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
            },
            leadingIcon = selected?.let { category ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
            .background(Surface, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = date.format(dateFormat),
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
        Icon(
            imageVector = Icons.Outlined.CalendarToday,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(18.dp)
        )
    }

    if (showPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
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
