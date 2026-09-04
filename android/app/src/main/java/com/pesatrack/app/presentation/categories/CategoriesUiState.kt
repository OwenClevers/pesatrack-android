package com.pesatrack.app.presentation.categories

import com.pesatrack.app.domain.model.Category

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true
)
