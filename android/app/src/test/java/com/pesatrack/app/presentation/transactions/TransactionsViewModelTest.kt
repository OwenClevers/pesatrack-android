package com.pesatrack.app.presentation.transactions

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.fake.FakeCategoryRepository
import com.pesatrack.app.fake.FakeTransactionRepository
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {

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
    fun `groups transactions by day and sorts groups newest first across a month boundary`() = runTest {
        val janThirtyFirstMorning = LocalDateTime.of(2026, 1, 31, 9, 0)
        val janThirtyFirstEvening = LocalDateTime.of(2026, 1, 31, 20, 0)
        val febFirst = LocalDateTime.of(2026, 2, 1, 8, 0)
        val transactions = listOf(
            tx(1, 100.0, date = janThirtyFirstMorning),
            tx(2, 50.0, date = janThirtyFirstEvening),
            tx(3, 200.0, date = febFirst)
        )
        val viewModel = TransactionsViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food))
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val groups = viewModel.uiState.value.groups
        assertEquals(2, groups.size)

        // The February group sorts ahead of January, even though 1 < 31.
        assertEquals("1 Feb 2026", groups[0].label)
        assertEquals(listOf(febFirst), groups[0].transactions.map { it.transactionDate })

        assertEquals("31 Jan 2026", groups[1].label)
        // Within a day, transactions sort newest first too.
        assertEquals(
            listOf(janThirtyFirstEvening, janThirtyFirstMorning),
            groups[1].transactions.map { it.transactionDate }
        )

        job.cancel()
    }

    private fun tx(
        id: Long,
        amount: Double,
        date: LocalDateTime
    ) = Transaction(
        id = id,
        amount = amount,
        type = TransactionType.EXPENSE,
        categoryId = food.id,
        merchant = null,
        description = null,
        transactionDate = date,
        source = TransactionSource.MANUAL
    )
}
