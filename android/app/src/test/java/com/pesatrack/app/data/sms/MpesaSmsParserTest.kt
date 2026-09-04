package com.pesatrack.app.data.sms

import com.pesatrack.app.domain.model.TransactionType
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MpesaSmsParserTest {

    private val parser = MpesaSmsParser()

    @Test
    fun `sent to person is parsed as expense`() {
        val message = "QGH7XXXXX1 Confirmed. Ksh500.00 sent to JOHN KAMAU 0712345678 on 4/9/26 " +
            "at 2:15 PM. New M-PESA balance is Ksh1,234.00. Transaction cost, Ksh0.00."

        val result = parser.parse(message)

        assertEquals(
            MpesaSmsTransaction(
                transactionCode = "QGH7XXXXX1",
                amount = 500.00,
                counterparty = "JOHN KAMAU",
                timestamp = LocalDateTime.of(2026, 9, 4, 14, 15),
                type = TransactionType.EXPENSE
            ),
            result
        )
    }

    @Test
    fun `paid to till business is parsed as expense`() {
        val message = "QGH7XXXXX2 Confirmed. Ksh2,500.00 paid to NAIROBI JAVA HOUSE. on 4/9/26 " +
            "at 1:05 PM. New M-PESA balance is Ksh3,200.00."

        val result = parser.parse(message)

        assertEquals(
            MpesaSmsTransaction(
                transactionCode = "QGH7XXXXX2",
                amount = 2500.00,
                counterparty = "NAIROBI JAVA HOUSE",
                timestamp = LocalDateTime.of(2026, 9, 4, 13, 5),
                type = TransactionType.EXPENSE
            ),
            result
        )
    }

    @Test
    fun `paid to paybill with account number is parsed as expense`() {
        val message = "QGH7XXXXX3 Confirmed. Ksh1,200.00 paid to KPLC PREPAID for account 12345678 " +
            "on 4/9/26 at 10:00 AM. New M-PESA balance is Ksh500.00."

        val result = parser.parse(message)

        assertEquals(
            MpesaSmsTransaction(
                transactionCode = "QGH7XXXXX3",
                amount = 1200.00,
                counterparty = "KPLC PREPAID",
                timestamp = LocalDateTime.of(2026, 9, 4, 10, 0),
                type = TransactionType.EXPENSE
            ),
            result
        )
    }

    @Test
    fun `withdrawn from agent is parsed as expense`() {
        val message = "QGH7XXXXX4 Confirmed. You have withdrawn Ksh3,000.00 from agent 123456 - " +
            "JOHN AGENT DOE on 4/9/26 at 11:20 AM. New M-PESA balance is Ksh2,000.00. " +
            "Transaction cost, Ksh29.00."

        val result = parser.parse(message)

        assertEquals(
            MpesaSmsTransaction(
                transactionCode = "QGH7XXXXX4",
                amount = 3000.00,
                counterparty = "JOHN AGENT DOE",
                timestamp = LocalDateTime.of(2026, 9, 4, 11, 20),
                type = TransactionType.EXPENSE
            ),
            result
        )
    }

    @Test
    fun `withdrawn from agent without the word agent is parsed as expense`() {
        val message = "QGH7XXXXX9 Confirmed. You have withdrawn Ksh1,000.00 from 654321 - " +
            "JANE AGENT on 4/9/26 at 4:45 PM. New M-PESA balance is Ksh900.00."

        val result = parser.parse(message)

        assertEquals(
            MpesaSmsTransaction(
                transactionCode = "QGH7XXXXX9",
                amount = 1000.00,
                counterparty = "JANE AGENT",
                timestamp = LocalDateTime.of(2026, 9, 4, 16, 45),
                type = TransactionType.EXPENSE
            ),
            result
        )
    }

    @Test
    fun `received money is parsed as income`() {
        val message = "QGH7XXXXX5 Confirmed. You have received Ksh1,500.00 from JANE DOE 0722334455 " +
            "on 4/9/26 at 9:00 AM. New M-PESA balance is Ksh4,500.00."

        val result = parser.parse(message)

        assertEquals(
            MpesaSmsTransaction(
                transactionCode = "QGH7XXXXX5",
                amount = 1500.00,
                counterparty = "JANE DOE",
                timestamp = LocalDateTime.of(2026, 9, 4, 9, 0),
                type = TransactionType.INCOME
            ),
            result
        )
    }

    @Test
    fun `midnight and noon times are parsed correctly`() {
        val midnight = "QGH7XXXXX6 Confirmed. You have received Ksh100.00 from JANE DOE " +
            "on 4/9/26 at 12:00 AM. New M-PESA balance is Ksh100.00."
        val noon = "QGH7XXXXX7 Confirmed. You have received Ksh100.00 from JANE DOE " +
            "on 4/9/26 at 12:00 PM. New M-PESA balance is Ksh100.00."

        assertEquals(LocalDateTime.of(2026, 9, 4, 0, 0), parser.parse(midnight)?.timestamp)
        assertEquals(LocalDateTime.of(2026, 9, 4, 12, 0), parser.parse(noon)?.timestamp)
    }

    @Test
    fun `unrelated sms returns null`() {
        val message = "Your OTP is 123456. Do not share this code with anyone."

        assertNull(parser.parse(message))
    }

    @Test
    fun `mpesa-like message missing the decimal amount returns null`() {
        val message = "QGH7XXXXX8 Confirmed. Ksh500 sent to JOHN DOE on 4/9/26 at 2:15 PM."

        assertNull(parser.parse(message))
    }

    @Test
    fun `blank message returns null`() {
        assertNull(parser.parse(""))
        assertNull(parser.parse("   "))
    }

    @Test
    fun `truncated mpesa message returns null`() {
        val message = "QGH7XXXXX0 Confirmed. Ksh500.00 sent to JOHN DOE"

        assertNull(parser.parse(message))
    }
}
