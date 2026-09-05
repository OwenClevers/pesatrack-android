package com.pesatrack.app.data.backup

import com.pesatrack.app.domain.model.Budget
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.model.TransactionSource
import com.pesatrack.app.domain.model.TransactionType
import java.time.LocalDateTime
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupSerializerTest {

    private val food = Category(id = 1, name = "Food", iconKey = "food", colorKey = "food")
    private val transport = Category(id = 2, name = "Transport", iconKey = "transport", colorKey = "transport")

    @Test
    fun `round-trips a full payload through serialize and deserialize`() {
        val payload = BackupPayload(
            schemaVersion = BackupSerializer.CURRENT_SCHEMA_VERSION,
            exportedAt = LocalDateTime.of(2026, 9, 5, 10, 30),
            categories = listOf(food, transport),
            transactions = listOf(
                Transaction(
                    id = 1,
                    amount = 500.0,
                    type = TransactionType.EXPENSE,
                    categoryId = food.id,
                    merchant = "Naivas Supermarket",
                    description = "Groceries",
                    transactionDate = LocalDateTime.of(2026, 9, 4, 14, 15),
                    source = TransactionSource.MPESA_SMS,
                    createdAt = LocalDateTime.of(2026, 9, 4, 14, 15, 5),
                    smsCode = "QGH7XXXXX1"
                ),
                Transaction(
                    id = 2,
                    amount = 1500.0,
                    type = TransactionType.INCOME,
                    categoryId = transport.id,
                    merchant = null,
                    description = null,
                    transactionDate = LocalDateTime.of(2026, 9, 3, 9, 0),
                    source = TransactionSource.MANUAL,
                    createdAt = LocalDateTime.of(2026, 9, 3, 9, 0, 2),
                    smsCode = null
                )
            ),
            budgets = listOf(
                Budget(id = 1, categoryId = food.id, limit = 10000.0, month = YearMonth.of(2026, 9))
            )
        )

        val json = BackupSerializer.serialize(payload)
        val roundTripped = BackupSerializer.deserialize(json)

        assertEquals(payload, roundTripped)
    }

    @Test
    fun `null merchant, description, createdAt, and smsCode survive the round trip as null`() {
        val payload = BackupPayload(
            schemaVersion = BackupSerializer.CURRENT_SCHEMA_VERSION,
            exportedAt = LocalDateTime.of(2026, 9, 5, 10, 30),
            categories = listOf(food),
            transactions = listOf(
                Transaction(
                    id = 1,
                    amount = 200.0,
                    type = TransactionType.EXPENSE,
                    categoryId = food.id,
                    merchant = null,
                    description = null,
                    transactionDate = LocalDateTime.of(2026, 9, 4, 14, 15),
                    source = TransactionSource.MANUAL,
                    createdAt = null,
                    smsCode = null
                )
            ),
            budgets = emptyList()
        )

        val roundTripped = BackupSerializer.deserialize(BackupSerializer.serialize(payload))
        val transaction = roundTripped.transactions.single()

        assertEquals(null, transaction.merchant)
        assertEquals(null, transaction.description)
        assertEquals(null, transaction.createdAt)
        assertEquals(null, transaction.smsCode)
    }

    @Test
    fun `rejects a file with no schema version`() {
        assertThrows(BackupFormatException::class.java) {
            BackupSerializer.deserialize("""{"categories":[],"transactions":[],"budgets":[]}""")
        }
    }

    @Test
    fun `rejects a schema version newer than this app supports`() {
        val future = """
            {
              "schemaVersion": ${BackupSerializer.CURRENT_SCHEMA_VERSION + 1},
              "exportedAt": "2026-09-05T10:30",
              "categories": [],
              "transactions": [],
              "budgets": []
            }
        """.trimIndent()

        assertThrows(BackupFormatException::class.java) {
            BackupSerializer.deserialize(future)
        }
    }

    @Test
    fun `rejects something that isn't JSON at all`() {
        assertThrows(BackupFormatException::class.java) {
            BackupSerializer.deserialize("not json")
        }
    }
}
