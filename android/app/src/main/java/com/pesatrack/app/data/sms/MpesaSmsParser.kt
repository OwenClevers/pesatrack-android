package com.pesatrack.app.data.sms

import com.pesatrack.app.domain.model.TransactionType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Parses Safaricom M-Pesa confirmation SMS bodies into [ParsedSmsTransaction].
 * Pure Kotlin, no Android dependencies, so it's testable as a plain JVM unit.
 */
class MpesaSmsParser : SmsParser {

    override val senderPattern: String = "MPESA"

    override fun parse(message: String): ParsedSmsTransaction? {
        val body = message.trim()
        if (body.isEmpty()) return null

        for (spec in patterns) {
            val match = spec.regex.find(body) ?: continue
            return toTransaction(match, spec.type) ?: continue
        }
        return null
    }

    private fun toTransaction(match: MatchResult, type: TransactionType): ParsedSmsTransaction? {
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

        return ParsedSmsTransaction(
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

        // Real messages normally show two decimals (Ksh500.00), but some message
        // variants show whole-shilling amounts with none at all (Ksh500) -- the
        // decimal part is optional so both parse to the same Double.
        const val AMOUNT_VALUE = """[\d,]+(?:\.\d{1,2})?"""

        // Letters, digits, spaces, and the punctuation that shows up in real
        // Kenyan person and business names: periods, apostrophes, commas,
        // ampersands, hyphens and slashes.
        const val NAME_CHARS = """[A-Za-z0-9 .,'&/-]+?"""

        // "QGH7XXXXX1 Confirmed. Ksh500.00 sent to JOHN KAMAU 0712345678 on 4/9/26 at 2:15 PM. ..."
        val SENT_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*Ksh(?<amount>$AMOUNT_VALUE)\s+sent to\s+""" +
                """(?<name>$NAME_CHARS)(?:\s+\d{9,12})?\s+$DATE_TIME_SUFFIX""",
            RegexOption.IGNORE_CASE
        )

        // "QGH7XXXXX2 Confirmed. Ksh2,500.00 paid to NAIROBI JAVA HOUSE. on 4/9/26 at 1:05 PM. ..."
        // "QGH7XXXXX3 Confirmed. Ksh1,200.00 paid to KPLC PREPAID for account 12345678 on 4/9/26 at 10:00 AM. ..."
        val PAID_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*Ksh(?<amount>$AMOUNT_VALUE)\s+paid to\s+""" +
                """(?<name>$NAME_CHARS)\.?\s*(?:for account\s+\S+\s+)?$DATE_TIME_SUFFIX""",
            RegexOption.IGNORE_CASE
        )

        // "QGH7XXXXX4 Confirmed. You have withdrawn Ksh3,000.00 from agent 123456 - JOHN AGENT DOE on 4/9/26 at 11:20 AM. ..."
        val WITHDRAW_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*You have withdrawn\s+Ksh(?<amount>$AMOUNT_VALUE)\s+""" +
                """from\s+(?:agent\s+)?\d+\s*-\s*(?<name>$NAME_CHARS)\s+$DATE_TIME_SUFFIX""",
            RegexOption.IGNORE_CASE
        )

        // "QGH7XXXXX5 Confirmed. You have received Ksh1,500.00 from JANE DOE 0722334455 on 4/9/26 at 9:00 AM. ..."
        val RECEIVED_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*You have received\s+Ksh(?<amount>$AMOUNT_VALUE)\s+""" +
                """from\s+(?<name>$NAME_CHARS)(?:\s+\d{9,12})?\s+$DATE_TIME_SUFFIX""",
            RegexOption.IGNORE_CASE
        )

        // "QGH7XXXXX6 Confirmed. You bought Ksh100.00 of Airtime on 4/9/26 at 2:15 PM. ..."
        // No counterparty name in this message shape, so the "name" group
        // captures the fixed literal word "Airtime" itself rather than free-form
        // text -- keeps toTransaction()'s group handling identical for every
        // pattern instead of needing a separate fallback path for name-less ones.
        val AIRTIME_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*You bought\s+Ksh(?<amount>$AMOUNT_VALUE)\s+of\s+""" +
                """(?<name>Airtime)\s+$DATE_TIME_SUFFIX""",
            RegexOption.IGNORE_CASE
        )

        // "QGH7XXXXX7 Confirmed. Fuliza M-PESA amount used to complete this transaction is Ksh150.00 on 4/9/26 at 2:15 PM. ..."
        // Fuliza is M-PESA's overdraft product, so this covers both.
        val FULIZA_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*(?<name>Fuliza M-PESA)\s+amount used to complete """ +
                """this transaction is\s+Ksh(?<amount>$AMOUNT_VALUE)\s+$DATE_TIME_SUFFIX""",
            RegexOption.IGNORE_CASE
        )

        // "QGH7XXXXX8 Confirmed. Reversal of Ksh500.00 from JOHN KAMAU is complete on 4/9/26 at 2:15 PM. ..."
        val REVERSAL_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*Reversal of\s+Ksh(?<amount>$AMOUNT_VALUE)\s+""" +
                """from\s+(?<name>$NAME_CHARS)\s+is complete\s+$DATE_TIME_SUFFIX""",
            RegexOption.IGNORE_CASE
        )

        // "QGH7XXXXX9 Confirmed. You have received a refund of Ksh500.00 from NAIROBI JAVA HOUSE on 4/9/26 at 2:15 PM. ..."
        // The extra "a refund of" between "received" and "Ksh" means this never
        // collides with RECEIVED_REGEX (which requires "received" to be followed
        // immediately by "Ksh"), so ordering relative to it doesn't matter.
        val REFUND_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*You have received a refund of\s+""" +
                """Ksh(?<amount>$AMOUNT_VALUE)\s+from\s+(?<name>$NAME_CHARS)\s+$DATE_TIME_SUFFIX""",
            RegexOption.IGNORE_CASE
        )

        // "QGH7XXXXX0 Confirmed. Ksh500.00 sent to JOHN'S SHOP for Pochi la Biashara on 4/9/26 at 2:15 PM. ..."
        // Must be tried before SENT_REGEX: SENT_REGEX's non-greedy name group has
        // no literal text of its own to stop at, so against a Pochi message it
        // would keep expanding straight through "for Pochi la Biashara" (all
        // letters/spaces, which its character class allows) and only stop once
        // it reaches "on <date>" -- swallowing "for Pochi la Biashara" into the
        // counterparty. This dedicated pattern's own literal "for Pochi la
        // Biashara" segment gives the name group a real place to stop instead.
        val POCHI_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*Ksh(?<amount>$AMOUNT_VALUE)\s+sent to\s+""" +
                """(?<name>$NAME_CHARS)\s+for Pochi la Biashara\s+$DATE_TIME_SUFFIX""",
            RegexOption.IGNORE_CASE
        )

        val patterns = listOf(
            PatternSpec(REFUND_REGEX, TransactionType.INCOME),
            PatternSpec(RECEIVED_REGEX, TransactionType.INCOME),
            PatternSpec(REVERSAL_REGEX, TransactionType.INCOME),
            PatternSpec(FULIZA_REGEX, TransactionType.EXPENSE),
            PatternSpec(AIRTIME_REGEX, TransactionType.EXPENSE),
            PatternSpec(WITHDRAW_REGEX, TransactionType.EXPENSE),
            PatternSpec(POCHI_REGEX, TransactionType.EXPENSE),
            PatternSpec(PAID_REGEX, TransactionType.EXPENSE),
            PatternSpec(SENT_REGEX, TransactionType.EXPENSE)
        )
    }
}
