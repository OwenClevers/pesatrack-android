package com.pesatrack.app.fake

import com.pesatrack.app.domain.model.Budget
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.CategoryDeleteResult
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.repository.BudgetRepository
import com.pesatrack.app.domain.repository.CategoryRepository
import com.pesatrack.app.domain.repository.TransactionRepository
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [TransactionRepository] fake for ViewModel unit tests. */
class FakeTransactionRepository(
    initial: List<Transaction> = emptyList()
) : TransactionRepository {

    private val transactions = MutableStateFlow(initial)

    override fun getTransactions(): Flow<List<Transaction>> = transactions

    override fun getTransaction(id: Long): Flow<Transaction?> =
        transactions.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun addTransaction(transaction: Transaction) {
        transactions.update { it + transaction }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactions.update { list -> list.map { if (it.id == transaction.id) transaction else it } }
    }

    override suspend fun deleteTransaction(id: Long) {
        transactions.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun importMpesaTransaction(transaction: Transaction, smsCode: String): Boolean {
        if (transactions.value.any { it.smsCode == smsCode }) return false
        transactions.update { it + transaction }
        return true
    }

    override suspend fun replaceAll(transactions: List<Transaction>) {
        this.transactions.value = transactions
    }
}

/** In-memory [CategoryRepository] fake for ViewModel unit tests. */
class FakeCategoryRepository(
    initial: List<Category> = emptyList()
) : CategoryRepository {

    private val categories = MutableStateFlow(initial)

    override fun getCategories(): Flow<List<Category>> = categories

    override suspend fun addCategory(name: String, iconKey: String, colorKey: String): Category {
        val category = Category(
            id = (categories.value.maxOfOrNull { it.id } ?: 0) + 1,
            name = name,
            iconKey = iconKey,
            colorKey = colorKey
        )
        categories.update { it + category }
        return category
    }

    override suspend fun updateCategory(id: Long, name: String, iconKey: String, colorKey: String) {
        categories.update { list ->
            list.map { if (it.id == id) it.copy(name = name, iconKey = iconKey, colorKey = colorKey) else it }
        }
    }

    override suspend fun deleteCategory(id: Long): CategoryDeleteResult {
        categories.update { list -> list.filterNot { it.id == id } }
        return CategoryDeleteResult.Deleted
    }

    override suspend fun replaceAll(categories: List<Category>) {
        this.categories.value = categories
    }
}

/** In-memory [BudgetRepository] fake for ViewModel unit tests. */
class FakeBudgetRepository(
    initial: List<Budget> = emptyList()
) : BudgetRepository {

    private val budgets = MutableStateFlow(initial)

    override fun getBudgets(month: YearMonth): Flow<List<Budget>> =
        budgets.map { list -> list.filter { it.month == month } }

    override suspend fun getAllBudgets(): List<Budget> = budgets.value

    override suspend fun upsertBudget(budget: Budget) {
        val resolvedId = if (budget.id != 0L) {
            budget.id
        } else {
            (budgets.value.maxOfOrNull { it.id } ?: 0) + 1
        }
        budgets.update { list ->
            list.filterNot { it.id == resolvedId || (it.categoryId == budget.categoryId && it.month == budget.month) } +
                budget.copy(id = resolvedId)
        }
    }

    override suspend fun deleteBudget(id: Long) {
        budgets.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun replaceAll(budgets: List<Budget>) {
        this.budgets.value = budgets
    }
}
