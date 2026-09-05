package com.pesatrack.app.presentation.transactions

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionType
import java.time.LocalDate

data class TransactionsUiState(
    val groups: List<TransactionGroup> = emptyList(),
    val categoriesById: Map<Long, Category> = emptyMap(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val filter: TransactionFilter = TransactionFilter(),
    // Non-empty means selection mode is active -- long-pressing a row starts
    // it, tapping other rows adds to it, and it's cleared explicitly rather
    // than by navigating away (there's nowhere else selection would persist to).
    val selectedIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true
)

data class TransactionGroup(
    val label: String,
    val transactions: List<Transaction>
)

// type/categoryId null means "all"; startDate/endDate null means unbounded.
data class TransactionFilter(
    val type: TransactionType? = null,
    val categoryId: Long? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
) {
    val isActive: Boolean
        get() = type != null || categoryId != null || startDate != null || endDate != null
}
