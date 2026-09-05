package com.pesatrack.app.data.backup

import com.pesatrack.app.domain.repository.BudgetRepository
import com.pesatrack.app.domain.repository.CategoryRepository
import com.pesatrack.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

enum class RestoreMode { MERGE, REPLACE }

data class RestoreResult(
    val importedCategories: Int,
    val importedTransactions: Int,
    val skippedTransactions: Int,
    val importedBudgets: Int
)

/**
 * Gathers/restores a full-database [BackupPayload] via the repository interfaces
 * only, so it's testable with the same fakes used for ViewModel tests -- no Room.
 */
class BackupManager(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
) {

    suspend fun createBackup(): BackupPayload =
        BackupPayload(
            schemaVersion = BackupSerializer.CURRENT_SCHEMA_VERSION,
            exportedAt = LocalDateTime.now(),
            categories = categoryRepository.getCategories().first(),
            transactions = transactionRepository.getTransactions().first(),
            budgets = budgetRepository.getAllBudgets()
        )

    suspend fun restore(payload: BackupPayload, mode: RestoreMode): RestoreResult =
        when (mode) {
            RestoreMode.REPLACE -> restoreByReplacing(payload)
            RestoreMode.MERGE -> restoreByMerging(payload)
        }

    // Wipes everything currently on the device and inserts the backup's rows as-is
    // (original ids preserved), since categoryId references in the backup already
    // point at the categories being restored alongside them.
    private suspend fun restoreByReplacing(payload: BackupPayload): RestoreResult {
        categoryRepository.replaceAll(payload.categories)
        transactionRepository.replaceAll(payload.transactions)
        budgetRepository.replaceAll(payload.budgets)
        return RestoreResult(
            importedCategories = payload.categories.size,
            importedTransactions = payload.transactions.size,
            skippedTransactions = 0,
            importedBudgets = payload.budgets.size
        )
    }

    // Keeps everything already on the device and adds what's new from the backup.
    // Categories are matched by name (case-insensitive) -- a match reuses the
    // existing category's id instead of creating a duplicate. Transactions are
    // matched by smsCode where present (the same unique-index dedup import
    // already relies on); transactions without an smsCode have no dedup key and
    // are always added. Budgets upsert by (categoryId, month), so importing the
    // same budget twice just updates its limit rather than duplicating it.
    private suspend fun restoreByMerging(payload: BackupPayload): RestoreResult {
        val existingByName = categoryRepository.getCategories().first()
            .associateBy { it.name.trim().lowercase() }

        var importedCategories = 0
        val categoryIdMap = mutableMapOf<Long, Long>()
        for (category in payload.categories) {
            val existing = existingByName[category.name.trim().lowercase()]
            categoryIdMap[category.id] = if (existing != null) {
                existing.id
            } else {
                importedCategories++
                categoryRepository.addCategory(category.name, category.iconKey, category.colorKey).id
            }
        }

        var importedTransactions = 0
        var skippedTransactions = 0
        for (transaction in payload.transactions) {
            val remapped = transaction.copy(
                id = 0,
                categoryId = categoryIdMap[transaction.categoryId] ?: transaction.categoryId
            )
            val smsCode = transaction.smsCode
            val inserted = if (smsCode != null) {
                transactionRepository.importMpesaTransaction(remapped, smsCode)
            } else {
                transactionRepository.addTransaction(remapped)
                true
            }
            if (inserted) importedTransactions++ else skippedTransactions++
        }

        var importedBudgets = 0
        for (budget in payload.budgets) {
            val remapped = budget.copy(
                id = 0,
                categoryId = categoryIdMap[budget.categoryId] ?: budget.categoryId
            )
            budgetRepository.upsertBudget(remapped)
            importedBudgets++
        }

        return RestoreResult(
            importedCategories = importedCategories,
            importedTransactions = importedTransactions,
            skippedTransactions = skippedTransactions,
            importedBudgets = importedBudgets
        )
    }
}
