package com.pesatrack.app.presentation.transactions

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesatrack.app.core.BudgetAlertPreferences
import com.pesatrack.app.data.budget.BudgetAlertChecker
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.domain.repository.CategoryRepository
import com.pesatrack.app.domain.repository.MerchantCategoryRepository
import com.pesatrack.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

class AddTransactionViewModel(
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val merchantCategoryRepository: MerchantCategoryRepository,
    private val budgetAlertChecker: BudgetAlertChecker,
    private val context: Context,
    private val transactionId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState(isEditing = transactionId != null))
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getCategories().collect { categories ->
                _uiState.update { state ->
                    val resolved = state.category
                        ?: state.pendingCategoryId?.let { id -> categories.firstOrNull { it.id == id } }
                    state.copy(categories = categories, category = resolved)
                }
            }
        }

        if (transactionId != null) {
            viewModelScope.launch {
                val transaction = repository.getTransaction(transactionId).first() ?: return@launch
                _uiState.update { state ->
                    state.copy(
                        amountText = formatAmountText(transaction.amount),
                        type = transaction.type,
                        category = state.categories.firstOrNull { it.id == transaction.categoryId },
                        pendingCategoryId = transaction.categoryId,
                        merchant = transaction.merchant.orEmpty(),
                        description = transaction.description.orEmpty(),
                        date = transaction.transactionDate.toLocalDate(),
                        time = transaction.transactionDate.toLocalTime(),
                        transactionId = transaction.id,
                        createdAt = transaction.createdAt,
                        source = transaction.source,
                        smsCode = transaction.smsCode
                    )
                }
            }
        }
    }

    // BigDecimal(amount) (the binary value) would surface floating-point noise
    // (e.g. 12345678.9 -> ...90000000000745...); parsing amount.toString() keeps
    // Double's own shortest decimal representation instead.
    private fun formatAmountText(amount: Double): String =
        if (amount == amount.toLong().toDouble()) {
            amount.toLong().toString()
        } else {
            BigDecimal(amount.toString()).stripTrailingZeros().toPlainString()
        }

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
            val transaction = Transaction(
                id = state.transactionId,
                amount = amount,
                type = state.type,
                categoryId = category.id,
                merchant = state.merchant.ifBlank { null },
                description = state.description.ifBlank { null },
                transactionDate = state.date.atTime(if (state.isEditing) state.time else LocalTime.now()),
                source = state.source,
                createdAt = state.createdAt,
                smsCode = state.smsCode
            )
            if (state.isEditing) {
                repository.updateTransaction(transaction)
            } else {
                repository.addTransaction(transaction)
            }
            // The user just told us this merchant belongs in this category --
            // remember it so future imports/entries for the same merchant
            // classify correctly without needing another correction.
            transaction.merchant?.let { merchant ->
                merchantCategoryRepository.learn(merchant, category.id)
            }
            if (transaction.type == TransactionType.EXPENSE) {
                budgetAlertChecker.check(
                    category.id,
                    YearMonth.from(transaction.transactionDate),
                    BudgetAlertPreferences.enabledThresholds(context)
                )
            }
            _uiState.update { it.copy(isSaving = false, saveComplete = true) }
        }
    }

    class Factory(
        private val repository: TransactionRepository,
        private val categoryRepository: CategoryRepository,
        private val merchantCategoryRepository: MerchantCategoryRepository,
        private val budgetAlertChecker: BudgetAlertChecker,
        context: Context,
        private val transactionId: Long? = null
    ) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AddTransactionViewModel(
                repository,
                categoryRepository,
                merchantCategoryRepository,
                budgetAlertChecker,
                appContext,
                transactionId
            ) as T
    }
}
