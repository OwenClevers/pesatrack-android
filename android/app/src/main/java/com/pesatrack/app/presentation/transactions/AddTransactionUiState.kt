package com.pesatrack.app.presentation.transactions

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.TransactionType
import java.time.LocalDate

data class AddTransactionUiState(
    val amountText: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: Category? = null,
    val categories: List<Category> = emptyList(),
    val merchant: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false
) {
    val amount: Double? get() = amountText.toDoubleOrNull()

    val canSave: Boolean get() = (amount ?: 0.0) > 0.0 && category != null && !isSaving
}
