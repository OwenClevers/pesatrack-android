package com.pesatrack.app.data.backup

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupManagerTest {

    private val food = Category(id = 1, name = "Food", iconKey = "food", colorKey = "food")

    @Test
    fun `merge matches an existing category by name case-insensitively instead of duplicating`() = runTest {
        val categoryRepository = FakeCategoryRepository(listOf(food))
        val manager = BackupManager(FakeTransactionRepository(), categoryRepository, FakeBudgetRepository())

        // Backup's "Food" has a different id (as it would from another device)
        // and different casing -- should still resolve to the existing category.
        val backupFood = Category(id = 99, name = "food", iconKey = "food", colorKey = "food")
        val payload = payloadOf(categories = listOf(backupFood))

        val result = manager.restore(payload, RestoreMode.MERGE)

        assertEquals(0, result.importedCategories)
        assertEquals(listOf(food), categoryRepository.getCategories().first())
    }

    @Test
    fun `merge adds a category that doesn't match any existing name`() = runTest {
        val categoryRepository = FakeCategoryRepository(listOf(food))
        val manager = BackupManager(FakeTransactionRepository(), categoryRepository, FakeBudgetRepository())

        val payload = payloadOf(categories = listOf(Category(id = 99, name = "Travel", iconKey = "other", colorKey = "other")))

        val result = manager.restore(payload, RestoreMode.MERGE)

        assertEquals(1, result.importedCategories)
        assertTrue(categoryRepository.getCategories().first().any { it.name == "Travel" })
    }

    @Test
    fun `merge skips a transaction whose smsCode already exists`() = runTest {
        val existing = tx(1, 500.0, smsCode = "QGH7XXXXX1")
        val transactionRepository = FakeTransactionRepository(listOf(existing))
        val manager = BackupManager(transactionRepository, FakeCategoryRepository(listOf(food)), FakeBudgetRepository())

        val payload = payloadOf(
            categories = listOf(food),
            transactions = listOf(tx(2, 999.0, smsCode = "QGH7XXXXX1"))
        )

        val result = manager.restore(payload, RestoreMode.MERGE)

        assertEquals(0, result.importedTransactions)
        assertEquals(1, result.skippedTransactions)
        assertEquals(listOf(500.0), transactionRepository.getTransactions().first().map { it.amount })
    }

    @Test
    fun `merge always adds transactions with no smsCode, even if it duplicates a previous merge`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val manager = BackupManager(transactionRepository, FakeCategoryRepository(listOf(food)), FakeBudgetRepository())
        val payload = payloadOf(categories = listOf(food), transactions = listOf(tx(1, 300.0, smsCode = null)))

        manager.restore(payload, RestoreMode.MERGE)
        val secondResult = manager.restore(payload, RestoreMode.MERGE)

        assertEquals(1, secondResult.importedTransactions)
        assertEquals(0, secondResult.skippedTransactions)
        assertEquals(2, transactionRepository.getTransactions().first().size)
    }

    @Test
    fun `merge remaps an imported transaction's categoryId to the matched existing category`() = runTest {
        val transactionRepository = FakeTransactionRepository()
        val categoryRepository = FakeCategoryRepository(listOf(food))
        val manager = BackupManager(transactionRepository, categoryRepository, FakeBudgetRepository())

        // The backup's Food category has a different id than this device's Food.
        val backupFood = Category(id = 42, name = "Food", iconKey = "food", colorKey = "food")
        val payload = payloadOf(
            categories = listOf(backupFood),
            transactions = listOf(tx(1, 300.0, categoryId = backupFood.id, smsCode = null))
        )

        manager.restore(payload, RestoreMode.MERGE)

        val stored = transactionRepository.getTransactions().first().single()
        assertEquals(food.id, stored.categoryId)
    }

    @Test
    fun `merge upserts a budget by category and month rather than duplicating`() = runTest {
        val month = YearMonth.of(2026, 9)
        val existingBudget = Budget(id = 1, categoryId = food.id, limit = 5000.0, month = month)
        val budgetRepository = FakeBudgetRepository(listOf(existingBudget))
        val manager = BackupManager(FakeTransactionRepository(), FakeCategoryRepository(listOf(food)), budgetRepository)

        val payload = payloadOf(
            categories = listOf(food),
            budgets = listOf(Budget(id = 99, categoryId = food.id, limit = 8000.0, month = month))
        )

        manager.restore(payload, RestoreMode.MERGE)

        val budgets = budgetRepository.getAllBudgets()
        assertEquals(1, budgets.size)
        assertEquals(8000.0, budgets.single().limit, 0.001)
    }

    @Test
    fun `replace wipes existing data and inserts the backup's rows as-is`() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(tx(1, 111.0, smsCode = null)))
        val categoryRepository = FakeCategoryRepository(listOf(food))
        val budgetRepository = FakeBudgetRepository(
            listOf(Budget(id = 1, categoryId = food.id, limit = 100.0, month = YearMonth.of(2026, 1)))
        )
        val manager = BackupManager(transactionRepository, categoryRepository, budgetRepository)

        val backupCategory = Category(id = 7, name = "Travel", iconKey = "other", colorKey = "other")
        val payload = payloadOf(
            categories = listOf(backupCategory),
            transactions = listOf(tx(3, 777.0, categoryId = backupCategory.id, smsCode = null)),
            budgets = listOf(Budget(id = 5, categoryId = backupCategory.id, limit = 200.0, month = YearMonth.of(2026, 9)))
        )

        val result = manager.restore(payload, RestoreMode.REPLACE)

        assertEquals(1, result.importedCategories)
        assertEquals(1, result.importedTransactions)
        assertEquals(1, result.importedBudgets)
        assertEquals(listOf(backupCategory), categoryRepository.getCategories().first())
        assertEquals(listOf(777.0), transactionRepository.getTransactions().first().map { it.amount })
        assertEquals(listOf(200.0), budgetRepository.getAllBudgets().map { it.limit })
    }

    private fun payloadOf(
        categories: List<Category> = emptyList(),
        transactions: List<Transaction> = emptyList(),
        budgets: List<Budget> = emptyList()
    ) = BackupPayload(
        schemaVersion = BackupSerializer.CURRENT_SCHEMA_VERSION,
        exportedAt = LocalDateTime.of(2026, 9, 5, 10, 0),
        categories = categories,
        transactions = transactions,
        budgets = budgets
    )

    private fun tx(
        id: Long,
        amount: Double,
        categoryId: Long = food.id,
        smsCode: String?
    ) = Transaction(
        id = id,
        amount = amount,
        type = TransactionType.EXPENSE,
        categoryId = categoryId,
        merchant = null,
        description = null,
        transactionDate = LocalDateTime.of(2026, 9, 4, 9, 0),
        source = if (smsCode != null) TransactionSource.MPESA_SMS else TransactionSource.MANUAL,
        createdAt = LocalDateTime.of(2026, 9, 4, 9, 0),
        smsCode = smsCode
    )
}
