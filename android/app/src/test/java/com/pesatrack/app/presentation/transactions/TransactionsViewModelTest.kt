package com.pesatrack.app.presentation.transactions

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.fake.FakeCategoryRepository
import com.pesatrack.app.fake.FakeMerchantCategoryRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {

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
            FakeCategoryRepository(listOf(food)),
            FakeMerchantCategoryRepository()
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

    @Test
    fun `search matches merchant case-insensitively`() = runTest {
        val transactions = listOf(
            tx(1, 500.0, merchant = "Naivas Supermarket"),
            tx(2, 300.0, merchant = "Java House")
        )
        val viewModel = TransactionsViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            FakeMerchantCategoryRepository()
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("naivas")
        advanceUntilIdle()

        val results = viewModel.uiState.value.groups.flatMap { it.transactions }
        assertEquals(listOf(1L), results.map { it.id })

        job.cancel()
    }

    @Test
    fun `search matches description case-insensitively`() = runTest {
        val transactions = listOf(
            tx(1, 500.0, merchant = "Shop", description = "Weekly Groceries"),
            tx(2, 300.0, merchant = "Shop", description = "Electronics")
        )
        val viewModel = TransactionsViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            FakeMerchantCategoryRepository()
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("groceries")
        advanceUntilIdle()

        val results = viewModel.uiState.value.groups.flatMap { it.transactions }
        assertEquals(listOf(1L), results.map { it.id })

        job.cancel()
    }

    @Test
    fun `search with no matches shows empty groups`() = runTest {
        val transactions = listOf(tx(1, 500.0, merchant = "Naivas Supermarket"))
        val viewModel = TransactionsViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            FakeMerchantCategoryRepository()
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("nonexistent")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.groups.isEmpty())

        job.cancel()
    }

    @Test
    fun `filter by type keeps only expenses or only income`() = runTest {
        val transactions = listOf(
            tx(1, 500.0, type = TransactionType.EXPENSE),
            tx(2, 1000.0, type = TransactionType.INCOME)
        )
        val viewModel = TransactionsViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            FakeMerchantCategoryRepository()
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onFilterChange(TransactionFilter(type = TransactionType.INCOME))
        advanceUntilIdle()

        val results = viewModel.uiState.value.groups.flatMap { it.transactions }
        assertEquals(listOf(2L), results.map { it.id })

        job.cancel()
    }

    @Test
    fun `filter by category keeps only matching category`() = runTest {
        val transactions = listOf(
            tx(1, 500.0, categoryId = food.id),
            tx(2, 300.0, categoryId = transport.id)
        )
        val viewModel = TransactionsViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food, transport)),
            FakeMerchantCategoryRepository()
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onFilterChange(TransactionFilter(categoryId = transport.id))
        advanceUntilIdle()

        val results = viewModel.uiState.value.groups.flatMap { it.transactions }
        assertEquals(listOf(2L), results.map { it.id })

        job.cancel()
    }

    @Test
    fun `filter by date range excludes transactions outside the range`() = runTest {
        val transactions = listOf(
            tx(1, 100.0, date = LocalDateTime.of(2026, 3, 1, 9, 0)),
            tx(2, 200.0, date = LocalDateTime.of(2026, 3, 10, 9, 0)),
            tx(3, 300.0, date = LocalDateTime.of(2026, 3, 20, 9, 0))
        )
        val viewModel = TransactionsViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            FakeMerchantCategoryRepository()
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onFilterChange(
            TransactionFilter(
                startDate = LocalDate.of(2026, 3, 5),
                endDate = LocalDate.of(2026, 3, 15)
            )
        )
        advanceUntilIdle()

        val results = viewModel.uiState.value.groups.flatMap { it.transactions }
        assertEquals(listOf(2L), results.map { it.id })

        job.cancel()
    }

    @Test
    fun `search and filter combine so both criteria must match`() = runTest {
        val transactions = listOf(
            // Matches search but not the type filter.
            tx(1, 500.0, merchant = "Naivas Supermarket", type = TransactionType.INCOME, categoryId = food.id),
            // Matches both.
            tx(2, 300.0, merchant = "Naivas Express", type = TransactionType.EXPENSE, categoryId = food.id),
            // Matches the type filter but not search.
            tx(3, 200.0, merchant = "Java House", type = TransactionType.EXPENSE, categoryId = food.id)
        )
        val viewModel = TransactionsViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            FakeMerchantCategoryRepository()
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("naivas")
        viewModel.onFilterChange(TransactionFilter(type = TransactionType.EXPENSE))
        advanceUntilIdle()

        val results = viewModel.uiState.value.groups.flatMap { it.transactions }
        assertEquals(listOf(2L), results.map { it.id })

        job.cancel()
    }

    @Test
    fun `toggling selection adds and removes ids`() = runTest {
        val transactions = listOf(tx(1, 500.0), tx(2, 300.0))
        val viewModel = TransactionsViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            FakeMerchantCategoryRepository()
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onToggleSelected(1)
        viewModel.onToggleSelected(2)
        advanceUntilIdle()
        assertEquals(setOf(1L, 2L), viewModel.uiState.value.selectedIds)

        viewModel.onToggleSelected(1)
        advanceUntilIdle()
        assertEquals(setOf(2L), viewModel.uiState.value.selectedIds)

        job.cancel()
    }

    @Test
    fun `clearSelection empties the selected ids`() = runTest {
        val transactions = listOf(tx(1, 500.0), tx(2, 300.0))
        val viewModel = TransactionsViewModel(
            FakeTransactionRepository(transactions),
            FakeCategoryRepository(listOf(food)),
            FakeMerchantCategoryRepository()
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onToggleSelected(1)
        viewModel.onToggleSelected(2)
        advanceUntilIdle()

        viewModel.clearSelection()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.selectedIds.isEmpty())

        job.cancel()
    }

    @Test
    fun `assignCategoryToSelected updates every selected transaction and clears selection`() = runTest {
        val transactions = listOf(
            tx(1, 500.0, categoryId = food.id, merchant = "Naivas"),
            tx(2, 300.0, categoryId = food.id, merchant = "Java House"),
            tx(3, 200.0, categoryId = food.id, merchant = "Shell")
        )
        val transactionRepository = FakeTransactionRepository(transactions)
        val merchantCategoryRepository = FakeMerchantCategoryRepository()
        val viewModel = TransactionsViewModel(
            transactionRepository,
            FakeCategoryRepository(listOf(food, transport)),
            merchantCategoryRepository
        )
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onToggleSelected(1)
        viewModel.onToggleSelected(2)
        advanceUntilIdle()

        viewModel.assignCategoryToSelected(transport.id)
        advanceUntilIdle()

        val updated = viewModel.uiState.value.groups.flatMap { it.transactions }.associateBy { it.id }
        assertEquals(transport.id, updated.getValue(1).categoryId)
        assertEquals(transport.id, updated.getValue(2).categoryId)
        // Unselected transaction is untouched.
        assertEquals(food.id, updated.getValue(3).categoryId)

        assertEquals(transport.id, merchantCategoryRepository.getCategoryId("Naivas"))
        assertEquals(transport.id, merchantCategoryRepository.getCategoryId("Java House"))
        assertTrue(viewModel.uiState.value.selectedIds.isEmpty())

        job.cancel()
    }

    private fun tx(
        id: Long,
        amount: Double,
        type: TransactionType = TransactionType.EXPENSE,
        categoryId: Long = food.id,
        merchant: String? = null,
        description: String? = null,
        date: LocalDateTime = LocalDateTime.of(2026, 1, 31, 9, 0)
    ) = Transaction(
        id = id,
        amount = amount,
        type = type,
        categoryId = categoryId,
        merchant = merchant,
        description = description,
        transactionDate = date,
        source = TransactionSource.MANUAL
    )
}
