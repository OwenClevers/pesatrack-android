package com.pesatrack.app.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.repository.CategoryRepository
import com.pesatrack.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val groupDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy")

class TransactionsViewModel(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val filter = MutableStateFlow(TransactionFilter())

    val uiState: StateFlow<TransactionsUiState> =
        combine(
            transactionRepository.getTransactions(),
            categoryRepository.getCategories(),
            searchQuery,
            filter
        ) { transactions, categories, query, criteria ->
            transactions.toTransactionsState(categories, query, criteria)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionsUiState()
        )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onFilterChange(criteria: TransactionFilter) {
        filter.value = criteria
    }

    private fun List<Transaction>.toTransactionsState(
        categories: List<Category>,
        query: String,
        criteria: TransactionFilter
    ): TransactionsUiState {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val trimmedQuery = query.trim()

        val filtered = filter { transaction ->
            matchesSearch(transaction, trimmedQuery) && matchesFilter(transaction, criteria)
        }

        val groups = filtered.groupBy { it.transactionDate.toLocalDate() }
            .entries
            .sortedByDescending { it.key }
            .map { (date, transactions) ->
                TransactionGroup(
                    label = when (date) {
                        today -> "Today"
                        yesterday -> "Yesterday"
                        else -> date.format(groupDateFormat)
                    },
                    transactions = transactions.sortedByDescending { it.transactionDate }
                )
            }

        return TransactionsUiState(
            groups = groups,
            categoriesById = categories.associateBy { it.id },
            categories = categories,
            searchQuery = query,
            filter = criteria,
            isLoading = false
        )
    }

    private fun matchesSearch(transaction: Transaction, query: String): Boolean {
        if (query.isEmpty()) return true
        return transaction.merchant?.contains(query, ignoreCase = true) == true ||
            transaction.description?.contains(query, ignoreCase = true) == true
    }

    private fun matchesFilter(transaction: Transaction, criteria: TransactionFilter): Boolean {
        if (criteria.type != null && transaction.type != criteria.type) return false
        if (criteria.categoryId != null && transaction.categoryId != criteria.categoryId) return false
        val date = transaction.transactionDate.toLocalDate()
        if (criteria.startDate != null && date < criteria.startDate) return false
        if (criteria.endDate != null && date > criteria.endDate) return false
        return true
    }

    class Factory(
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TransactionsViewModel(transactionRepository, categoryRepository) as T
    }
}
