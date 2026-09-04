package com.pesatrack.app.data.sms

import com.pesatrack.app.domain.model.TransactionType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MpesaSmsTransaction(
    val transactionCode: String,
    val amount: Double,
    val counterparty: String,
    val timestamp: LocalDateTime,
    val type: TransactionType
)

/**
 * Parses Safaricom M-Pesa confirmation SMS bodies into [MpesaSmsTransaction].
 * Pure Kotlin, no Android dependencies, so it's testable as a plain JVM unit.
 */
class MpesaSmsParser {

    fun parse(message: String): MpesaSmsTransaction? {
        val body = message.trim()
        if (body.isEmpty()) return null

        for (spec in patterns) {
            val match = spec.regex.find(body) ?: continue
            return toTransaction(match, spec.type) ?: continue
        }
        return null
    }

    private fun toTransaction(match: MatchResult, type: TransactionType): MpesaSmsTransaction? {
        val groups = match.groups as MatchNamedGroupCollection

        val code = groups[GROUP_CODE]?.value?.trim() ?: return null
        val amountText = groups[GROUP_AMOUNT]?.value ?: return null
        val name = groups[GROUP_NAME]?.value ?: return null
        val date = groups[GROUP_DATE]?.value ?: return null
        val time = groups[GROUP_TIME]?.value ?: return null
        val meridiem = groups[GROUP_MERIDIEM]?.value ?: return null

        val amount = amountText.replace(",", "").toDoubleOrNull() ?: return null

        val timestamp = runCatching {
            LocalDateTime.parse("$date $time $meridiem".uppercase(Locale.ENGLISH), dateTimeFormatter)
        }.getOrNull() ?: return null

        val counterparty = name.trim().trimEnd('.').replace(WHITESPACE_REGEX, " ")
        if (counterparty.isEmpty()) return null

        return MpesaSmsTransaction(
            transactionCode = code,
            amount = amount,
            counterparty = counterparty,
            timestamp = timestamp,
            type = type
        )
    }

    private class PatternSpec(val regex: Regex, val type: TransactionType)

    private companion object {

        const val GROUP_CODE = "code"
        const val GROUP_AMOUNT = "amount"
        const val GROUP_NAME = "name"
        const val GROUP_DATE = "date"
        const val GROUP_TIME = "time"
        const val GROUP_MERIDIEM = "meridiem"

        val WHITESPACE_REGEX = Regex("\\s+")

        val dateTimeFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("d/M/yy h:mm a", Locale.ENGLISH)

        // Shared "on 4/9/26 at 2:15 PM" tail, reused by every message shape below.
        const val DATE_TIME_SUFFIX =
            """on\s+(?<date>\d{1,2}/\d{1,2}/\d{2})\s+at\s+(?<time>\d{1,2}:\d{2})\s*(?<meridiem>[APap][Mm])"""

        // "QGH7XXXXX1 Confirmed. Ksh500.00 sent to JOHN KAMAU 0712345678 on 4/9/26 at 2:15 PM. ..."
        val SENT_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*Ksh(?<amount>[\d,]+\.\d{2})\s+sent to\s+""" +
                """(?<name>[A-Za-z .'-]+?)(?:\s+\d{9,12})?\s+$DATE_TIME_SUFFIX""",
            RegexOption.IGNORE_CASE
        )

        // "QGH7XXXXX2 Confirmed. Ksh2,500.00 paid to NAIROBI JAVA HOUSE. on 4/9/26 at 1:05 PM. ..."
        // "QGH7XXXXX3 Confirmed. Ksh1,200.00 paid to KPLC PREPAID for account 12345678 on 4/9/26 at 10:00 AM. ..."
        val PAID_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*Ksh(?<amount>[\d,]+\.\d{2})\s+paid to\s+""" +
                """(?<name>[A-Za-z0-9 .'&-]+?)\.?\s*(?:for account\s+\S+\s+)?$DATE_TIME_SUFFIX""",
            RegexOption.IGNORE_CASE
        )

        // "QGH7XXXXX4 Confirmed. You have withdrawn Ksh3,000.00 from agent 123456 - JOHN AGENT DOE on 4/9/26 at 11:20 AM. ..."
        val WITHDRAW_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*You have withdrawn\s+Ksh(?<amount>[\d,]+\.\d{2})\s+""" +
                """from\s+(?:agent\s+)?\d+\s*-\s*(?<name>[A-Za-z0-9 .'&-]+?)\s+$DATE_TIME_SUFFIX""",
            RegexOption.IGNORE_CASE
        )

        // "QGH7XXXXX5 Confirmed. You have received Ksh1,500.00 from JANE DOE 0722334455 on 4/9/26 at 9:00 AM. ..."
        val RECEIVED_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*You have received\s+Ksh(?<amount>[\d,]+\.\d{2})\s+""" +
                """from\s+(?<name>[A-Za-z .'-]+?)(?:\s+\d{9,12})?\s+$DATE_TIME_SUFFIX""",
            RegexOption.IGNORE_CASE
        )

        val patterns = listOf(
            PatternSpec(RECEIVED_REGEX, TransactionType.INCOME),
            PatternSpec(WITHDRAW_REGEX, TransactionType.EXPENSE),
            PatternSpec(PAID_REGEX, TransactionType.EXPENSE),
            PatternSpec(SENT_REGEX, TransactionType.EXPENSE)
        )
    }
}
