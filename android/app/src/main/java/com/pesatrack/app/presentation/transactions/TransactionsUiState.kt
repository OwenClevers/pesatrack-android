package com.pesatrack.app.presentation.transactions

import com.pesatrack.app.domain.model.Transaction

data class TransactionsUiState(
    val groups: List<TransactionGroup> = emptyList(),
    val isLoading: Boolean = true
)

data class TransactionGroup(
    val label: String,
    val transactions: List<Transaction>
)
