package com.pesatrack.app.data.export

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvExporterTest {

    private val food = Category(id = 1, name = "Food", iconKey = "food", colorKey = "food")

    private fun tx(
        amount: Double = 500.0,
        type: TransactionType = TransactionType.EXPENSE,
        categoryId: Long = food.id,
        merchant: String? = "Naivas",
        description: String? = "Groceries",
        date: LocalDateTime = LocalDateTime.of(2026, 9, 4, 14, 15),
        source: TransactionSource = TransactionSource.MANUAL
    ) = Transaction(
        id = 1,
        amount = amount,
        type = type,
        categoryId = categoryId,
        merchant = merchant,
        description = description,
        transactionDate = date,
        source = source
    )

    @Test
    fun `renders the expected header row`() {
        val csv = CsvExporter.toCsv(emptyList(), emptyMap())

        assertEquals("Date,Type,Category,Merchant,Description,Amount,Source", csv)
    }

    @Test
    fun `renders a plain row with no special characters`() {
        val csv = CsvExporter.toCsv(listOf(tx()), mapOf(food.id to food))
        val dataRow = csv.lines()[1]

        assertEquals("4 Sep 2026 2:15 PM,Expense,Food,Naivas,Groceries,500.00,Manually added", dataRow)
    }

    @Test
    fun `quotes a merchant field containing a comma`() {
        val csv = CsvExporter.toCsv(
            listOf(tx(merchant = "Naivas, Karen Branch")),
            mapOf(food.id to food)
        )
        val dataRow = csv.lines()[1]

        assertEquals(true, dataRow.contains("\"Naivas, Karen Branch\""))
    }

    @Test
    fun `escapes and quotes a description field containing a double quote`() {
        val csv = CsvExporter.toCsv(
            listOf(tx(description = "Bought a \"deal\" item")),
            mapOf(food.id to food)
        )
        val dataRow = csv.lines()[1]

        assertEquals(true, dataRow.contains("\"Bought a \"\"deal\"\" item\""))
    }

    @Test
    fun `quotes a field containing both a comma and a quote without breaking columns`() {
        val csv = CsvExporter.toCsv(
            listOf(tx(merchant = "Java House, \"CBD\"", description = "Lunch, with a \"friend\"")),
            mapOf(food.id to food)
        )
        val dataRow = csv.lines()[1]
        val columns = parseCsvLine(dataRow)

        assertEquals(7, columns.size)
        assertEquals("Java House, \"CBD\"", columns[3])
        assertEquals("Lunch, with a \"friend\"", columns[4])
    }

    @Test
    fun `falls back to Other for a category id with no match`() {
        val csv = CsvExporter.toCsv(listOf(tx(categoryId = 999L)), emptyMap())
        val dataRow = csv.lines()[1]

        assertEquals(true, dataRow.contains(",Other,"))
    }

    @Test
    fun `blank merchant and description render as empty fields, not the word null`() {
        val csv = CsvExporter.toCsv(
            listOf(tx(merchant = null, description = null)),
            mapOf(food.id to food)
        )
        val dataRow = csv.lines()[1]

        assertEquals("4 Sep 2026 2:15 PM,Expense,Food,,,500.00,Manually added", dataRow)
    }

    @Test
    fun `income type renders as Income`() {
        val csv = CsvExporter.toCsv(
            listOf(tx(type = TransactionType.INCOME)),
            mapOf(food.id to food)
        )
        val dataRow = csv.lines()[1]

        assertEquals(true, dataRow.contains(",Income,"))
    }

    // Minimal RFC 4180 field parser (comma-separated, "" escapes a literal
    // quote inside a quoted field) -- just enough to verify escape() output
    // round-trips into the right number of columns with the right content.
    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                inQuotes && c == '"' && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        fields.add(current.toString())
        return fields
    }
}
