package com.pesatrack.app.presentation.dashboard

import com.pesatrack.app.domain.model.Budget
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.fake.FakeBudgetRepository
import com.pesatrack.app.fake.FakeCategoryRepository
import com.pesatrack.app.fake.FakeTransactionRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val food = Category(id = 1, name = "Food", iconKey = "food", colorKey = "food")
    private val transport = Category(id = 2, name = "Transport", iconKey = "transport", colorKey = "transport")

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `todaySpending sums only today's expenses, excluding income and other days`() = runTest {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val transactions = listOf(
            tx(1, 100.0, TransactionType.EXPENSE, date = today.atTime(9, 0)),
            tx(2, 50.0, TransactionType.EXPENSE, date = today.atTime(18, 30)),
            tx(3, 200.0, TransactionType.INCOME, date = today.atTime(12, 0)),
            tx(4, 999.0, TransactionType.EXPENSE, date = yesterday.atTime(9, 0))
        )
        val viewModel = DashboardViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            FakeBudgetRepository()
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals(150.0, viewModel.uiState.value.todaySpending, 0.001)

        job.cancel()
    }

    @Test
    fun `monthIncome sums only this month's income`() = runTest {
        val month = YearMonth.now()
        val transactions = listOf(
            tx(1, 500.0, TransactionType.INCOME, date = month.atDay(1).atTime(9, 0)),
            tx(2, 300.0, TransactionType.INCOME, date = month.atDay(2).atTime(9, 0)),
            tx(3, 1000.0, TransactionType.INCOME, date = month.minusMonths(1).atEndOfMonth().atTime(9, 0)),
            tx(4, 400.0, TransactionType.EXPENSE, date = month.atDay(1).atTime(10, 0))
        )
        val viewModel = DashboardViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            FakeBudgetRepository()
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals(800.0, viewModel.uiState.value.monthIncome, 0.001)

        job.cancel()
    }

    @Test
    fun `remainingBudget is null when no budgets exist`() = runTest {
        val viewModel = DashboardViewModel(
            FakeTransactionRepository(emptyList()),
            FakeCategoryRepository(listOf(food)),
            FakeBudgetRepository(emptyList())
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.remainingBudget)

        job.cancel()
    }

    @Test
    fun `remainingBudget subtracts this month's category spend from total limits`() = runTest {
        val month = YearMonth.now()
        val budgets = listOf(
            Budget(id = 1, categoryId = food.id, limit = 1000.0, month = month),
            Budget(id = 2, categoryId = transport.id, limit = 500.0, month = month)
        )
        val transactions = listOf(
            tx(1, 200.0, TransactionType.EXPENSE, categoryId = food.id, date = month.atDay(1).atTime(9, 0)),
            tx(2, 100.0, TransactionType.EXPENSE, categoryId = transport.id, date = month.atDay(2).atTime(9, 0)),
            // outside the selected month -- must not count
            tx(3, 50.0, TransactionType.EXPENSE, categoryId = food.id, date = month.minusMonths(1).atDay(1).atTime(9, 0)),
            // income in a budgeted category -- must not count
            tx(4, 9999.0, TransactionType.INCOME, categoryId = food.id, date = month.atDay(1).atTime(10, 0))
        )
        val viewModel = DashboardViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food, transport)),
            FakeBudgetRepository(budgets)
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertEquals(1200.0, viewModel.uiState.value.remainingBudget!!, 0.001)

        job.cancel()
    }

    @Test
    fun `recentTransactions caps at 5 and sorts newest first`() = runTest {
        val month = YearMonth.now()
        val transactions = (1..7).map { day ->
            tx(day.toLong(), 10.0 * day, TransactionType.EXPENSE, date = month.atDay(day).atTime(9, 0))
        }
        val viewModel = DashboardViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            FakeBudgetRepository()
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val recent = viewModel.uiState.value.recentTransactions
        assertEquals(5, recent.size)
        assertEquals(listOf(7L, 6L, 5L, 4L, 3L), recent.map { it.id })

        job.cancel()
    }

    private fun tx(
        id: Long,
        amount: Double,
        type: TransactionType,
        categoryId: Long = food.id,
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
