package com.pesatrack.app.presentation.transactions

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction

data class TransactionDetailsUiState(
    val transaction: Transaction? = null,
    val category: Category? = null,
    val isLoading: Boolean = true
)
