package com.pesatrack.app.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class AddTransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun onAmountChange(value: String) {
        _uiState.update { it.copy(amountText = value) }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { it.copy(type = type) }
    }

    fun onCategoryChange(category: Category) {
        _uiState.update { it.copy(category = category) }
    }

    fun onMerchantChange(value: String) {
        _uiState.update { it.copy(merchant = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun save() {
        val state = _uiState.value
        val amount = state.amount
        val category = state.category
        if (!state.canSave || amount == null || category == null) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    id = 0,
                    amount = amount,
                    type = state.type,
                    categoryId = category.id,
                    merchant = state.merchant.ifBlank { null },
                    description = state.description.ifBlank { null },
                    transactionDate = state.date.atTime(LocalTime.now()),
                    source = TransactionSource.MANUAL
                )
            )
            _uiState.update { it.copy(isSaving = false, saveComplete = true) }
        }
    }

    class Factory(
        private val repository: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AddTransactionViewModel(repository) as T
    }
}
