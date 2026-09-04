package com.pesatrack.app.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.repository.CategoryRepository
import com.pesatrack.app.domain.repository.TransactionRepository
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

    val uiState: StateFlow<TransactionsUiState> =
        combine(
            transactionRepository.getTransactions(),
            categoryRepository.getCategories()
        ) { transactions, categories -> transactions.toTransactionsState(categories) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TransactionsUiState()
            )

    private fun List<Transaction>.toTransactionsState(categories: List<Category>): TransactionsUiState {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val groups = groupBy { it.transactionDate.toLocalDate() }
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
            isLoading = false
        )
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
