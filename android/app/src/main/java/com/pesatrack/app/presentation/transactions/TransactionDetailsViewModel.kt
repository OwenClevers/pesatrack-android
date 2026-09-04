package com.pesatrack.app.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.repository.CategoryRepository
import com.pesatrack.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionDetailsViewModel(
    private val transactionId: Long,
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<TransactionDetailsUiState> =
        combine(
            transactionRepository.getTransaction(transactionId),
            categoryRepository.getCategories()
        ) { transaction, categories ->
            TransactionDetailsUiState(
                transaction = transaction,
                category = transaction?.let { t ->
                    categories.firstOrNull { it.id == t.categoryId } ?: Category.unknown(t.categoryId)
                },
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionDetailsUiState()
        )

    fun delete() {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transactionId)
        }
    }

    class Factory(
        private val transactionId: Long,
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TransactionDetailsViewModel(transactionId, transactionRepository, categoryRepository) as T
    }
}
