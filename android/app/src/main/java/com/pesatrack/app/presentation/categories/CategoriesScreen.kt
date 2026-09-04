package com.pesatrack.app.presentation.categories

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.pesatrack.app.domain.model.CategoryDeleteResult
import com.pesatrack.app.ui.theme.Divider
import com.pesatrack.app.ui.theme.Expense
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.PrimaryDark
import com.pesatrack.app.ui.theme.StatusBarIcons
import com.pesatrack.app.ui.theme.Surface
import com.pesatrack.app.ui.theme.TextPrimary
import com.pesatrack.app.ui.theme.TextSecondary
import com.pesatrack.app.ui.theme.components.CategoryIconKeys
import com.pesatrack.app.ui.theme.components.CategoryColorKeys
import com.pesatrack.app.ui.theme.components.EmptyState
import com.pesatrack.app.ui.theme.components.PesaBottomBar
import com.pesatrack.app.ui.theme.components.categoryColorSwatch
import com.pesatrack.app.ui.theme.components.categoryIcon
import com.pesatrack.app.ui.theme.components.visual
import kotlinx.coroutines.launch

private const val DEFAULT_CATEGORY_KEY = "other"

private data class CategorySheetTarget(
    val id: Long?,
    val initialName: String,
    val initialIconKey: String = DEFAULT_CATEGORY_KEY,
    val initialColorKey: String = DEFAULT_CATEGORY_KEY
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(navController: NavController) {
    val context = LocalContext.current
    val categoryRepository = remember { AppModule.provideCategoryRepository(context) }
    val viewModel: CategoriesViewModel = viewModel(
        factory = CategoriesViewModel.Factory(categoryRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var sheetTarget by remember { mutableStateOf<CategorySheetTarget?>(null) }
    var blockedMessage by remember { mutableStateOf<String?>(null) }

    StatusBarIcons(darkIcons = false)

    Scaffold(
        bottomBar = { PesaBottomBar(navController) },
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { sheetTarget = CategorySheetTarget(id = null, initialName = "") }) {
                        Icon(Icons.Outlined.AddCircle, contentDescription = "Add category")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Surface)
        ) {
            if (uiState.categories.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.AddCircle,
                    text = "No categories yet",
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                uiState.categories.forEachIndexed { index, category ->
                    CategoryRow(
                        category = category,
                        onClick = {
                            sheetTarget = CategorySheetTarget(
                                id = category.id,
                                initialName = category.name,
                                initialIconKey = category.iconKey,
                                initialColorKey = category.colorKey
                            )
                        }
                    )
                    if (index != uiState.categories.lastIndex) {
                        HorizontalDivider(color = Divider)
                    }
                }
            }
        }

        sheetTarget?.let { target ->
            val sheetState = rememberModalBottomSheetState()
            var name by remember(target) { mutableStateOf(target.initialName) }
            var iconKey by remember(target) { mutableStateOf(target.initialIconKey) }
            var colorKey by remember(target) { mutableStateOf(target.initialColorKey) }

            ModalBottomSheet(
                onDismissRequest = { sheetTarget = null },
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
                        text = if (target.id == null) "New category" else "Rename category",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Icon",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CategoryIconKeys.forEach { key ->
                                IconOption(
                                    iconKey = key,
                                    selected = key == iconKey,
                                    onClick = { iconKey = key }
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Color",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CategoryColorKeys.forEach { key ->
                                ColorOption(
                                    colorKey = key,
                                    selected = key == colorKey,
                                    onClick = { colorKey = key }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        if (target.id != null) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        when (val result = viewModel.deleteCategory(target.id)) {
                                            is CategoryDeleteResult.Deleted -> {
                                                sheetTarget = null
                                            }
                                            is CategoryDeleteResult.Blocked -> {
                                                blockedMessage =
                                                    "Can't delete this category — it's used by ${result.transactionCount} " +
                                                        if (result.transactionCount == 1) "transaction." else "transactions."
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Expense),
                                border = BorderStroke(1.dp, Expense)
                            ) {
                                Text("Delete")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.saveCategory(target.id, name.trim(), iconKey, colorKey)
                                sheetTarget = null
                            },
                            enabled = name.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                            border = BorderStroke(1.dp, Primary)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }

        blockedMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { blockedMessage = null },
                title = { Text("Can't delete category") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { blockedMessage = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun IconOption(
    iconKey: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Surface)
            .then(
                if (selected) Modifier.border(2.dp, Primary, CircleShape) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = categoryIcon(iconKey),
            contentDescription = iconKey,
            tint = if (selected) Primary else TextSecondary
        )
    }
}

@Composable
private fun ColorOption(
    colorKey: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(categoryColorSwatch(colorKey))
            .then(
                if (selected) Modifier.border(2.dp, TextPrimary, CircleShape) else Modifier
            )
            .clickable(onClick = onClick)
    )
}

@Composable
private fun CategoryRow(
    category: Category,
    onClick: () -> Unit
) {
    val visual = category.visual()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
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

        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary
        )
    }
}
