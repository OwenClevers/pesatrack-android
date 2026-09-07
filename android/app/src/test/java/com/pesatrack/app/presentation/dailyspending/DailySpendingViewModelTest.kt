package com.pesatrack.app.presentation.dailyspending

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.fake.FakeCategoryRepository
import com.pesatrack.app.fake.FakeTransactionRepository
import java.time.LocalDate
import java.time.LocalDateTime
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DailySpendingViewModelTest {

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
    fun `periodBars spans exactly 30 days ending today, oldest first, zero-filled on days with no spending`() = runTest {
        val today = LocalDate.now()
        val transactions = listOf(
            tx(1, 100.0, date = today.atTime(9, 0)),
            tx(2, 50.0, date = today.minusDays(29).atTime(9, 0)),
            // Outside the 30-day window -- must not appear as a bar.
            tx(3, 999.0, date = today.minusDays(30).atTime(9, 0))
        )
        val viewModel = DailySpendingViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            today
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val bars = viewModel.uiState.value.periodBars
        assertEquals(DAILY_SPENDING_PERIOD_DAYS, bars.size)
        assertEquals(today.minusDays(29), bars.first().date)
        assertEquals(today, bars.last().date)
        assertEquals(50.0, bars.first().amount, 0.001)
        assertEquals(100.0, bars.last().amount, 0.001)
        // A day with no transactions at all must still produce a zero point.
        assertEquals(0.0, bars[15].amount, 0.001)

        job.cancel()
    }

    @Test
    fun `selectedDayTotal and transactions reflect only the selected day, excluding income`() = runTest {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val transactions = listOf(
            tx(1, 200.0, date = yesterday.atTime(9, 0)),
            tx(2, 300.0, date = yesterday.atTime(18, 0)),
            tx(3, 999.0, date = yesterday.atTime(12, 0), type = TransactionType.INCOME),
            tx(4, 400.0, date = today.atTime(9, 0))
        )
        val viewModel = DailySpendingViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            yesterday
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(500.0, state.selectedDayTotal, 0.001)
        assertEquals(listOf(2L, 1L), state.selectedDayTransactions.map { it.id })
        assertTrue(state.selectedDayTransactions.all { it.type == TransactionType.EXPENSE })

        job.cancel()
    }

    @Test
    fun `onDaySelected switches the selected day without changing the period window`() = runTest {
        val today = LocalDate.now()
        val transactions = listOf(
            tx(1, 100.0, date = today.atTime(9, 0)),
            tx(2, 250.0, date = today.minusDays(5).atTime(9, 0))
        )
        val viewModel = DailySpendingViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            today
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onDaySelected(today.minusDays(5))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(today.minusDays(5), state.selectedDate)
        assertEquals(250.0, state.selectedDayTotal, 0.001)
        assertEquals(DAILY_SPENDING_PERIOD_DAYS, state.periodBars.size)
        assertEquals(today, state.periodBars.last().date)

        job.cancel()
    }

    @Test
    fun `comparisonPercent is null when the period average is zero`() = runTest {
        val today = LocalDate.now()
        val viewModel = DailySpendingViewModel(
            FakeTransactionRepository(emptyList()),
            FakeCategoryRepository(listOf(food)),
            today
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.comparisonPercent)

        job.cancel()
    }

    @Test
    fun `comparisonPercent reflects how far the selected day is above the period average`() = runTest {
        val today = LocalDate.now()
        // The only spending in the 30-day window is Ksh3000 on today, so the
        // average is 3000/30 = 100, and today is 2900% above that average.
        val transactions = listOf(tx(1, 3000.0, date = today.atTime(9, 0)))
        val viewModel = DailySpendingViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            today
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(100.0, state.periodAverage, 0.001)
        assertEquals(3000.0, state.selectedDayTotal, 0.001)
        assertEquals(2900, state.comparisonPercent)

        job.cancel()
    }

    private fun tx(
        id: Long,
        amount: Double,
        date: LocalDateTime,
        type: TransactionType = TransactionType.EXPENSE,
        categoryId: Long = food.id
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
