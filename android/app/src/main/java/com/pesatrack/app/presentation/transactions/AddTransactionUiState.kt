package com.pesatrack.app.presentation.transactions

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class AddTransactionUiState(
    val amountText: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: Category? = null,
    val categories: List<Category> = emptyList(),
    val merchant: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false,
    val isEditing: Boolean = false,
    val transactionId: Long = 0,
    val time: LocalTime = LocalTime.now(),
    val createdAt: LocalDateTime? = null,
    val source: TransactionSource = TransactionSource.MANUAL,
    val pendingCategoryId: Long? = null,
    val smsCode: String? = null
) {
    val amount: Double? get() = amountText.toDoubleOrNull()

    val canSave: Boolean get() = (amount ?: 0.0) > 0.0 && category != null && !isSaving
}
