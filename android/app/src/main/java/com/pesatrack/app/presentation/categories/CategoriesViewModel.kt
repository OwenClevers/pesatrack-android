package com.pesatrack.app.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesatrack.app.domain.model.CategoryDeleteResult
import com.pesatrack.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<CategoriesUiState> =
        categoryRepository.getCategories()
            .map { categories -> CategoriesUiState(categories = categories, isLoading = false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CategoriesUiState()
            )

    fun saveCategory(id: Long?, name: String) {
        viewModelScope.launch {
            if (id == null) {
                categoryRepository.addCategory(name)
            } else {
                categoryRepository.renameCategory(id, name)
            }
        }
    }

    suspend fun deleteCategory(id: Long): CategoryDeleteResult =
        categoryRepository.deleteCategory(id)

    class Factory(
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CategoriesViewModel(categoryRepository) as T
    }
}
