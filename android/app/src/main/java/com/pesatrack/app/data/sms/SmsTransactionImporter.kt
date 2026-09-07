package com.pesatrack.app.data.sms

import android.content.Context
import com.pesatrack.app.core.BudgetAlertPreferences
import com.pesatrack.app.data.budget.BudgetAlertChecker
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import com.pesatrack.app.domain.repository.CategoryRepository
import com.pesatrack.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.time.YearMonth

/**
 * Parses one already sender-matched SMS body and applies the same
 * classify -> insert -> budget-check pipeline whether it came from a bulk
 * [SmsReader] scan (MpesaImportViewModel) or a single live broadcast
 * (MpesaSmsReceiver), so the two call sites can't drift apart.
 */
class SmsTransactionImporter(
    private val parsers: List<SmsParser>,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val merchantCategorizer: MerchantCategorizer,
    private val budgetAlertChecker: BudgetAlertChecker,
    private val context: Context
) {

    sealed interface Result {
        data object FailedToParse : Result
        data object Duplicate : Result
        data class Imported(val transaction: Transaction) : Result
    }

    fun resolveParser(sender: String): SmsParser? =
        parsers.firstOrNull { sender.contains(it.senderPattern, ignoreCase = true) }

    suspend fun import(parser: SmsParser, body: String): Result {
        val parsed = parser.parse(body) ?: return Result.FailedToParse

        val categories = categoryRepository.getCategories().first()
        val transaction = Transaction(
            id = 0,
            amount = parsed.amount,
            type = parsed.type,
            categoryId = merchantCategorizer.classify(parsed.counterparty, categories),
            merchant = parsed.counterparty,
            description = null,
            transactionDate = parsed.timestamp,
            source = TransactionSource.MPESA_SMS
        )

        val inserted = transactionRepository.importMpesaTransaction(transaction, parsed.transactionCode)
        if (!inserted) return Result.Duplicate

        if (transaction.type == TransactionType.EXPENSE) {
            budgetAlertChecker.check(
                transaction.categoryId,
                YearMonth.from(transaction.transactionDate),
                BudgetAlertPreferences.enabledThresholds(context)
            )
        }
        return Result.Imported(transaction)
    }
}
