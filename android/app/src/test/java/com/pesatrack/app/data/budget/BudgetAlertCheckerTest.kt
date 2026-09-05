package com.pesatrack.app.data.budget

import com.pesatrack.app.domain.model.Budget
import com.pesatrack.app.domain.model.BudgetThreshold
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.fake.FakeBudgetAlertRepository
import com.pesatrack.app.fake.FakeBudgetRepository
import com.pesatrack.app.fake.FakeCategoryRepository
import com.pesatrack.app.fake.FakeTransactionRepository
import java.time.LocalDateTime
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetAlertCheckerTest {

    private val food = Category(id = 1, name = "Food", iconKey = "food", colorKey = "food")
    private val month = YearMonth.of(2026, 9)
    private val allThresholds = setOf(BudgetThreshold.WARNING, BudgetThreshold.EXCEEDED)

    private fun tx(amount: Double, day: Int = 1, categoryId: Long = food.id) = Transaction(
        id = 0,
        amount = amount,
        type = TransactionType.EXPENSE,
        categoryId = categoryId,
        merchant = null,
        description = null,
        transactionDate = LocalDateTime.of(2026, 9, day, 12, 0),
        source = TransactionSource.MANUAL
    )

    private class RecordingNotifier : BudgetAlertNotifier {
        data class Call(val categoryId: Long, val threshold: BudgetThreshold, val percent: Int)

        val calls = mutableListOf<Call>()
        var delivers = true

        override fun notify(category: Category, threshold: BudgetThreshold, percent: Int): Boolean {
            calls += Call(category.id, threshold, percent)
            return delivers
        }
    }

    private fun checker(
        transactions: List<Transaction> = emptyList(),
        budgets: List<Budget> = listOf(Budget(id = 1, categoryId = food.id, limit = 1000.0, month = month)),
        notifier: RecordingNotifier = RecordingNotifier()
    ) = BudgetAlertChecker(
        FakeBudgetRepository(budgets),
        FakeTransactionRepository(transactions),
        FakeCategoryRepository(listOf(food)),
        FakeBudgetAlertRepository(),
        notifier
    ) to notifier

    @Test
    fun `no budget set for the category fires nothing`() = runTest {
        val (checker, notifier) = checker(
            transactions = listOf(tx(900.0)),
            budgets = emptyList()
        )

        checker.check(food.id, month, allThresholds)

        assertTrue(notifier.calls.isEmpty())
    }

    @Test
    fun `spend below 80 percent fires nothing`() = runTest {
        val (checker, notifier) = checker(transactions = listOf(tx(700.0)))

        checker.check(food.id, month, allThresholds)

        assertTrue(notifier.calls.isEmpty())
    }

    @Test
    fun `crossing 80 percent fires a single warning`() = runTest {
        val (checker, notifier) = checker(transactions = listOf(tx(850.0)))

        checker.check(food.id, month, allThresholds)

        assertEquals(1, notifier.calls.size)
        assertEquals(BudgetThreshold.WARNING, notifier.calls[0].threshold)
        assertEquals(85, notifier.calls[0].percent)
    }

    @Test
    fun `checking again in the same month without new spend does not re-fire`() = runTest {
        val (checker, notifier) = checker(transactions = listOf(tx(850.0)))

        checker.check(food.id, month, allThresholds)
        checker.check(food.id, month, allThresholds)
        checker.check(food.id, month, allThresholds)

        assertEquals(1, notifier.calls.size)
    }

    @Test
    fun `a later transaction crossing 100 percent fires exceeded but not warning again`() = runTest {
        val transactionRepository = FakeTransactionRepository(listOf(tx(850.0, day = 1)))
        val notifier = RecordingNotifier()
        val checker = BudgetAlertChecker(
            FakeBudgetRepository(listOf(Budget(id = 1, categoryId = food.id, limit = 1000.0, month = month))),
            transactionRepository,
            FakeCategoryRepository(listOf(food)),
            FakeBudgetAlertRepository(),
            notifier
        )

        checker.check(food.id, month, allThresholds)
        assertEquals(listOf(BudgetThreshold.WARNING), notifier.calls.map { it.threshold })

        transactionRepository.addTransaction(tx(200.0, day = 2))
        checker.check(food.id, month, allThresholds)

        assertEquals(listOf(BudgetThreshold.WARNING, BudgetThreshold.EXCEEDED), notifier.calls.map { it.threshold })
    }

    @Test
    fun `a single transaction jumping straight past 100 percent fires both once`() = runTest {
        val (checker, notifier) = checker(transactions = listOf(tx(1200.0)))

        checker.check(food.id, month, allThresholds)

        assertEquals(listOf(BudgetThreshold.WARNING, BudgetThreshold.EXCEEDED), notifier.calls.map { it.threshold })
    }

    @Test
    fun `a new month resets firing state for the same category`() = runTest {
        val notifier = RecordingNotifier()
        val alertStateRepository = FakeBudgetAlertRepository()
        val septBudget = Budget(id = 1, categoryId = food.id, limit = 1000.0, month = month)
        val octBudget = Budget(id = 2, categoryId = food.id, limit = 1000.0, month = month.plusMonths(1))
        val checker = BudgetAlertChecker(
            FakeBudgetRepository(listOf(septBudget, octBudget)),
            FakeTransactionRepository(
                listOf(
                    tx(900.0, day = 1),
                    Transaction(
                        id = 0,
                        amount = 900.0,
                        type = TransactionType.EXPENSE,
                        categoryId = food.id,
                        merchant = null,
                        description = null,
                        transactionDate = LocalDateTime.of(2026, 10, 1, 12, 0),
                        source = TransactionSource.MANUAL
                    )
                )
            ),
            FakeCategoryRepository(listOf(food)),
            alertStateRepository,
            notifier
        )

        checker.check(food.id, month, allThresholds)
        checker.check(food.id, month.plusMonths(1), allThresholds)

        assertEquals(2, notifier.calls.size)
        assertTrue(notifier.calls.all { it.threshold == BudgetThreshold.WARNING })
    }

    @Test
    fun `disabling a threshold suppresses it even when crossed`() = runTest {
        val (checker, notifier) = checker(transactions = listOf(tx(1200.0)))

        checker.check(food.id, month, setOf(BudgetThreshold.EXCEEDED))

        assertEquals(listOf(BudgetThreshold.EXCEEDED), notifier.calls.map { it.threshold })
    }

    @Test
    fun `empty enabled thresholds skips the check entirely`() = runTest {
        val (checker, notifier) = checker(transactions = listOf(tx(1200.0)))

        checker.check(food.id, month, emptySet())

        assertTrue(notifier.calls.isEmpty())
    }

    @Test
    fun `a notifier that fails to deliver does not mark the threshold fired`() = runTest {
        val notifier = RecordingNotifier().apply { delivers = false }
        val checker = BudgetAlertChecker(
            FakeBudgetRepository(listOf(Budget(id = 1, categoryId = food.id, limit = 1000.0, month = month))),
            FakeTransactionRepository(listOf(tx(850.0))),
            FakeCategoryRepository(listOf(food)),
            FakeBudgetAlertRepository(),
            notifier
        )

        checker.check(food.id, month, allThresholds)
        assertEquals(1, notifier.calls.size)

        // Permission granted later -- the same still-crossed threshold should
        // fire again since it was never actually delivered the first time.
        notifier.delivers = true
        checker.check(food.id, month, allThresholds)

        assertEquals(2, notifier.calls.size)
    }

    @Test
    fun `income transactions do not count toward spend`() = runTest {
        val (checker, notifier) = checker(
            transactions = listOf(
                Transaction(
                    id = 0,
                    amount = 5000.0,
                    type = TransactionType.INCOME,
                    categoryId = food.id,
                    merchant = null,
                    description = null,
                    transactionDate = LocalDateTime.of(2026, 9, 1, 12, 0),
                    source = TransactionSource.MANUAL
                )
            )
        )

        checker.check(food.id, month, allThresholds)

        assertTrue(notifier.calls.isEmpty())
    }
}
