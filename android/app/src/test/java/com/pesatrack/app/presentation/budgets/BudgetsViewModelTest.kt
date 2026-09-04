package com.pesatrack.app.presentation.budgets

import com.pesatrack.app.domain.model.Budget
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.fake.FakeBudgetRepository
import com.pesatrack.app.fake.FakeCategoryRepository
import com.pesatrack.app.fake.FakeTransactionRepository
import java.time.LocalDateTime
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// Health-tier boundaries mirror BudgetsScreen: percent >= 90 is danger (red),
// percent >= 60 is warning (amber), below that is healthy (green).
@OptIn(ExperimentalCoroutinesApi::class)
class BudgetsViewModelTest {

    private val food = Category(id = 1, name = "Food", iconKey = "food", colorKey = "food")

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `budget row spend excludes other categories, other months, and income`() = runTest {
        val month = YearMonth.now()
        val transport = Category(id = 2, name = "Transport", iconKey = "transport", colorKey = "transport")
        val budgets = listOf(Budget(id = 1, categoryId = food.id, limit = 1000.0, month = month))
        val transactions = listOf(
            tx(1, 300.0, TransactionType.EXPENSE, categoryId = food.id, date = month.atDay(1).atTime(9, 0)),
            tx(2, 200.0, TransactionType.EXPENSE, categoryId = food.id, date = month.atDay(2).atTime(9, 0)),
            // wrong category -- excluded
            tx(3, 500.0, TransactionType.EXPENSE, categoryId = transport.id, date = month.atDay(3).atTime(9, 0)),
            // wrong month -- excluded
            tx(4, 400.0, TransactionType.EXPENSE, categoryId = food.id, date = month.minusMonths(1).atDay(1).atTime(9, 0)),
            // income -- excluded
            tx(5, 999.0, TransactionType.INCOME, categoryId = food.id, date = month.atDay(1).atTime(10, 0))
        )
        val viewModel = BudgetsViewModel(
            FakeBudgetRepository(budgets),
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food, transport))
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val row = viewModel.uiState.value.rows.single()
        assertEquals(500.0, row.spent, 0.001)
        assertEquals(1000.0, row.limit, 0.001)

        job.cancel()
    }

    @Test
    fun `budget percent boundary just below the warning threshold is 59`() = runTest {
        assertEquals(59, percentFor(spent = 59.0, limit = 100.0))
    }

    @Test
    fun `budget percent boundary at the warning threshold is 60`() = runTest {
        assertEquals(60, percentFor(spent = 60.0, limit = 100.0))
    }

    @Test
    fun `budget percent boundary just below the danger threshold is 89`() = runTest {
        assertEquals(89, percentFor(spent = 89.0, limit = 100.0))
    }

    @Test
    fun `budget percent boundary at the danger threshold is 90`() = runTest {
        assertEquals(90, percentFor(spent = 90.0, limit = 100.0))
    }

    private fun TestScope.percentFor(spent: Double, limit: Double): Int {
        val month = YearMonth.now()
        val budgets = listOf(Budget(id = 1, categoryId = food.id, limit = limit, month = month))
        val transactions = listOf(
            tx(1, spent, TransactionType.EXPENSE, categoryId = food.id, date = month.atDay(1).atTime(9, 0))
        )
        val viewModel = BudgetsViewModel(
            FakeBudgetRepository(budgets),
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food))
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()
        val percent = viewModel.uiState.value.rows.single().percent
        job.cancel()
        return percent
    }

    private fun tx(
        id: Long,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        date: LocalDateTime
    ) = Transaction(
        id = id,
        amount = amount,
        type = type,
        categoryId = categoryId,
        merchant = null,
        description = null,
        transactionDate = date,
        source = TransactionSource.MANUAL
    )
}
