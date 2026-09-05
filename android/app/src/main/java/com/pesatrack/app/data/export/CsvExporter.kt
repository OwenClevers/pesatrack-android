package com.pesatrack.app.data.export

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Renders transactions to CSV (RFC 4180 escaping), for the Reports screen's
 * export feature. Pure Kotlin, no Android dependency, so it's testable as a
 * plain JVM unit like BackupSerializer.
 */
object CsvExporter {

    private val HEADER = listOf("Date", "Type", "Category", "Merchant", "Description", "Amount", "Source")
    // No comma in the pattern -- a comma here would force every date field to
    // be quoted under RFC 4180, for no benefit.
    private val dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy h:mm a", Locale.ENGLISH)

    fun toCsv(transactions: List<Transaction>, categoriesById: Map<Long, Category>): String {
        val rows = transactions
            .sortedByDescending { it.transactionDate }
            .map { transaction -> rowFor(transaction, categoriesById) }
        return (listOf(HEADER) + rows).joinToString("\r\n") { row ->
            row.joinToString(",") { field -> escape(field) }
        }
    }

    private fun rowFor(transaction: Transaction, categoriesById: Map<Long, Category>): List<String> =
        listOf(
            transaction.transactionDate.format(dateFormat),
            transaction.type.displayLabel(),
            categoriesById[transaction.categoryId]?.name ?: "Other",
            transaction.merchant.orEmpty(),
            transaction.description.orEmpty(),
            // Locale.US always -- a locale using ',' as the decimal separator
            // would otherwise collide with the CSV column delimiter.
            String.format(Locale.US, "%.2f", transaction.amount),
            transaction.source.displayLabel()
        )

    private fun TransactionType.displayLabel(): String = when (this) {
        TransactionType.INCOME -> "Income"
        TransactionType.EXPENSE -> "Expense"
    }

    private fun TransactionSource.displayLabel(): String = when (this) {
        TransactionSource.MANUAL -> "Manually added"
        TransactionSource.MPESA_SMS -> "M-Pesa SMS"
        TransactionSource.IMPORT -> "Imported"
        TransactionSource.BANK_SYNC -> "Bank sync"
    }

    // RFC 4180: quote a field if it contains the delimiter, a quote, or a line
    // break, doubling any quotes already inside it.
    private fun escape(field: String): String {
        val needsQuoting = field.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        if (!needsQuoting) return field
        return "\"" + field.replace("\"", "\"\"") + "\""
    }
}
