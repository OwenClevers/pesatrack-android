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
    fun `airtime purchase is parsed as expense`() {
        val message = "QGH7AIR0001 Confirmed. You bought Ksh100.00 of Airtime on 4/9/26 at 2:15 PM. " +
            "New M-PESA balance is Ksh900.00. Transaction cost, Ksh0.00."

        val result = parser.parse(message)

        assertEquals(
            MpesaSmsTransaction(
                transactionCode = "QGH7AIR0001",
                amount = 100.00,
                counterparty = "Airtime",
                timestamp = LocalDateTime.of(2026, 9, 4, 14, 15),
                type = TransactionType.EXPENSE
            ),
            result
        )
    }

    @Test
    fun `fuliza overdraft usage is parsed as expense`() {
        val message = "QGH7FUL0002 Confirmed. Fuliza M-PESA amount used to complete this transaction " +
            "is Ksh150.00 on 4/9/26 at 2:15 PM. Interest charged Ksh1.50. Total Fuliza M-PESA " +
            "outstanding amount is Ksh500.00 due on 15/9/26."

        val result = parser.parse(message)

        assertEquals(
            MpesaSmsTransaction(
                transactionCode = "QGH7FUL0002",
                amount = 150.00,
                counterparty = "Fuliza M-PESA",
                timestamp = LocalDateTime.of(2026, 9, 4, 14, 15),
                type = TransactionType.EXPENSE
            ),
            result
        )
    }

    @Test
    fun `reversal confirmation is parsed as income`() {
        val message = "QGH7REV0003 Confirmed. Reversal of Ksh500.00 from JOHN KAMAU is complete " +
            "on 4/9/26 at 2:15 PM. New M-PESA balance is Ksh1,500.00."

        val result = parser.parse(message)

        assertEquals(
            MpesaSmsTransaction(
                transactionCode = "QGH7REV0003",
                amount = 500.00,
                counterparty = "JOHN KAMAU",
                timestamp = LocalDateTime.of(2026, 9, 4, 14, 15),
                type = TransactionType.INCOME
            ),
            result
        )
    }

    @Test
    fun `refund confirmation is parsed as income`() {
        val message = "QGH7REF0004 Confirmed. You have received a refund of Ksh500.00 from " +
            "NAIROBI JAVA HOUSE on 4/9/26 at 2:15 PM. New M-PESA balance is Ksh1,500.00."

        val result = parser.parse(message)

        assertEquals(
            MpesaSmsTransaction(
                transactionCode = "QGH7REF0004",
                amount = 500.00,
                counterparty = "NAIROBI JAVA HOUSE",
                timestamp = LocalDateTime.of(2026, 9, 4, 14, 15),
                type = TransactionType.INCOME
            ),
            result
        )
    }

    @Test
    fun `pochi la biashara payment is parsed as expense`() {
        val message = "QGH7POC0005 Confirmed. Ksh500.00 sent to JOHN'S SHOP for Pochi la Biashara " +
            "on 4/9/26 at 2:15 PM. New M-PESA balance is Ksh1,500.00."

        val result = parser.parse(message)

        assertEquals(
            MpesaSmsTransaction(
                transactionCode = "QGH7POC0005",
                amount = 500.00,
                counterparty = "JOHN'S SHOP",
                timestamp = LocalDateTime.of(2026, 9, 4, 14, 15),
                type = TransactionType.EXPENSE
            ),
            result
        )
    }

    @Test
    fun `pochi la biashara payment is not mistaken for a plain sent-to payment`() {
        // Without the dedicated Pochi pattern taking priority, SENT_REGEX's
        // non-greedy name group would keep expanding through "for Pochi la
        // Biashara" (all letters and spaces) and swallow it into the name.
        val message = "QGH7POC0006 Confirmed. Ksh500.00 sent to JOHN'S SHOP for Pochi la Biashara " +
            "on 4/9/26 at 2:15 PM. New M-PESA balance is Ksh1,500.00."

        val result = parser.parse(message)

        assertEquals("JOHN'S SHOP", result?.counterparty)
    }

    @Test
    fun `amount without decimals is parsed correctly`() {
        val message = "QGH7XXXXX8 Confirmed. Ksh500 sent to JOHN DOE on 4/9/26 at 2:15 PM. " +
            "New M-PESA balance is Ksh1,000."

        val result = parser.parse(message)

        assertEquals(500.00, result?.amount)
        assertEquals("JOHN DOE", result?.counterparty)
    }

    @Test
    fun `amount without decimals and with a thousands separator is parsed correctly`() {
        val message = "QGH7WDR0007 Confirmed. You have withdrawn Ksh1,000 from agent 123456 - " +
            "JOHN AGENT DOE on 4/9/26 at 11:20 AM. New M-PESA balance is Ksh500."

        val result = parser.parse(message)

        assertEquals(1000.00, result?.amount)
    }

    @Test
    fun `name with an apostrophe is parsed correctly`() {
        val message = "QGH7APO0008 Confirmed. Ksh350.00 paid to O'BRIEN'S PHARMACY. on 4/9/26 " +
            "at 3:30 PM. New M-PESA balance is Ksh650.00."

        val result = parser.parse(message)

        assertEquals("O'BRIEN'S PHARMACY", result?.counterparty)
    }

    @Test
    fun `name with an ampersand and a comma is parsed correctly`() {
        val message = "QGH7AMP0009 Confirmed. Ksh4,000.00 paid to SMITH & SONS, LTD. on 4/9/26 " +
            "at 3:30 PM. New M-PESA balance is Ksh6,000.00."

        val result = parser.parse(message)

        assertEquals("SMITH & SONS, LTD", result?.counterparty)
    }

    @Test
    fun `unusual transaction cost line placement does not affect the parsed fields`() {
        // No space after the time, and the transaction cost line appears before
        // the balance line instead of after it -- neither should matter, since
        // the parser only reads up to the date and time.
        val message = "QGH7TRC0010 Confirmed. Ksh450.00 paid to CORNER SHOP. on 4/9/26 at 6:30 PM." +
            "Transaction cost, Ksh0.00.New M-PESA balance is Ksh700.00. " +
            "Amount you can transact within the day is Ksh499,550.00."

        val result = parser.parse(message)

        assertEquals(
            MpesaSmsTransaction(
                transactionCode = "QGH7TRC0010",
                amount = 450.00,
                counterparty = "CORNER SHOP",
                timestamp = LocalDateTime.of(2026, 9, 4, 18, 30),
                type = TransactionType.EXPENSE
            ),
            result
        )
    }

    @Test
    fun `unrelated sms returns null`() {
        val message = "Your OTP is 123456. Do not share this code with anyone."

        assertNull(parser.parse(message))
    }

    @Test
    fun `balance check message returns null`() {
        val message = "QGH7BAL0011 Confirmed. Your M-PESA balance was Ksh4,500.00 on 4/9/26 " +
            "at 8:00 PM."

        assertNull(parser.parse(message))
    }

    @Test
    fun `mini statement style balance message returns null`() {
        val message = "Your M-PESA balance was Ksh4,500.00 on 4/9/2026 8:00 PM. Alternatively, " +
            "transact using M-PESA app."

        assertNull(parser.parse(message))
    }

    @Test
    fun `failed transaction notice returns null`() {
        val message = "Failed. You do not have enough money in your M-PESA account to complete " +
            "this transaction. Please top up your account."

        assertNull(parser.parse(message))
    }

    @Test
    fun `failed transaction notice with a code prefix returns null`() {
        val message = "QGH7FAI0012 Failed. Insufficient funds in your M-PESA account."

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
