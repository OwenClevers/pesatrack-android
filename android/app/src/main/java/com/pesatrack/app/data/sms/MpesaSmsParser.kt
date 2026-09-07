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

        // A phone number in a counterparty position may have its middle digits
        // masked for privacy (e.g. "0717***822") -- allowing '*' alongside
        // digits here, rather than \d only, is what lets that still match as
        // the optional phone-number tail instead of falling through to (and
        // breaking) the name group, which can't contain '*'.
        const val PHONE_OR_MASKED = """[\d*]{9,12}"""

        // "QGH7XXXXX1 Confirmed. Ksh500.00 sent to JOHN KAMAU 0712345678 on 4/9/26 at 2:15 PM. ..."
        // "QGH7XXXXX1 Confirmed. Ksh360.00 sent to EVOPAY LIMITED for account 0299...;W7026 on 4/9/26 ..."
        // The "for account" clause mirrors PAID_REGEX's -- paybill-style
        // "sent to" payments (EVOPAY, SASAPAY, Lipa na KCB, fuel cards, etc.)
        // reference an account/reference string that can contain characters
        // (';', '#', '_') outside NAME_CHARS, so it needs the same \S+
        // treatment as an explicit optional clause rather than letting the
        // name group try (and fail) to absorb it.
        val SENT_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*Ksh(?<amount>$AMOUNT_VALUE)\s+sent to\s+""" +
                """(?<name>$NAME_CHARS)(?:\s+$PHONE_OR_MASKED)?\s*(?:for account\s+\S+\s+)?$DATE_TIME_SUFFIX""",
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

        // "QGH7WTD0001 Confirmed.on 4/9/26 at 2:15 PMWithdraw Ksh1,000.00 from 252343 - JOHN AGENT DOE New M-PESA balance is Ksh500.00. ..."
        // An older/alternate agent-withdrawal template: the date/time clause
        // comes before "Withdraw" instead of after, often with no space at
        // all around "Confirmed." or the meridiem -- hence the \s* (not \s+)
        // at both of those joins. Anchoring the name on the more specific
        // "New M-PESA balance" (rather than a bare "New") avoids truncating a
        // real agent/shop name that happens to start with "New".
        val WITHDRAW_DATE_FIRST_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*$DATE_TIME_SUFFIX\s*Withdraw\s+""" +
                """Ksh(?<amount>$AMOUNT_VALUE)\s+from\s+(?:agent\s+)?\d+\s*-\s*(?<name>$NAME_CHARS)\s+New M-PESA balance""",
            RegexOption.IGNORE_CASE
        )

        // "QGH7XXXXX5 Confirmed. You have received Ksh1,500.00 from JANE DOE 0722334455 on 4/9/26 at 9:00 AM. ..."
        // "QGH7XXXXX5 Confirmed.You have received Ksh500.00 from JANE DOE 0722***455 on 4/9/26 at 9:00 AM ..."
        val RECEIVED_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*You have received\s+Ksh(?<amount>$AMOUNT_VALUE)\s+""" +
                """from\s+(?<name>$NAME_CHARS)(?:\s+$PHONE_OR_MASKED)?\s+$DATE_TIME_SUFFIX""",
            RegexOption.IGNORE_CASE
        )

        // "QGH7XXXXX6 Confirmed. You bought Ksh100.00 of Airtime on 4/9/26 at 2:15 PM. ..."
        // "QGH7XXXXX6 confirmed.You bought Ksh200.00 of airtime for 254711431737 on 4/9/26 at 2:15 PM. ..."
        // No counterparty name in this message shape, so the "name" group
        // captures the fixed literal word "Airtime" itself rather than free-form
        // text -- keeps toTransaction()'s group handling identical for every
        // pattern instead of needing a separate fallback path for name-less ones.
        // The optional "for <phone>" clause covers buying airtime for a
        // different number than the account holder's own.
        val AIRTIME_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*You bought\s+Ksh(?<amount>$AMOUNT_VALUE)\s+of\s+""" +
                """(?<name>Airtime)(?:\s+for\s+\d+)?\s+$DATE_TIME_SUFFIX""",
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

        // "QGH7REV0010 confirmed. Reversal of transaction QGH7REV0009 has been successfully
        // reversed on 4/9/26 at 2:15 PM and Ksh500.00 is credited to/debited from your
        // M-PESA account. New M-PESA account balance is Ksh1,500.00."
        // A different reversal template than REVERSAL_REGEX above: it references
        // the *original* transaction's code rather than a counterparty, and can
        // either credit or debit the account depending on the direction of the
        // reversal, hence two patterns with fixed, opposite types. "name" is the
        // fixed literal "Reversal", like Airtime/Fuliza M-PESA, since there's no
        // person/business to key it to.
        val REVERSAL_CREDIT_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*(?<name>Reversal) of transaction\s+[A-Z0-9]{9,12}\s+""" +
                """has been successfully reversed\s+$DATE_TIME_SUFFIX\s+and\s+Ksh(?<amount>$AMOUNT_VALUE)\s+""" +
                """is credited to your M-PESA account""",
            RegexOption.IGNORE_CASE
        )

        val REVERSAL_DEBIT_REGEX = Regex(
            """^(?<code>[A-Z0-9]{9,12})\s+Confirmed\.?\s*(?<name>Reversal) of transaction\s+[A-Z0-9]{9,12}\s+""" +
                """has been successfully reversed\s+$DATE_TIME_SUFFIX\s+and\s+Ksh(?<amount>$AMOUNT_VALUE)\s+""" +
                """is debited from your M-PESA account""",
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
            PatternSpec(REVERSAL_CREDIT_REGEX, TransactionType.INCOME),
            PatternSpec(REVERSAL_DEBIT_REGEX, TransactionType.EXPENSE),
            PatternSpec(FULIZA_REGEX, TransactionType.EXPENSE),
            PatternSpec(AIRTIME_REGEX, TransactionType.EXPENSE),
            PatternSpec(WITHDRAW_REGEX, TransactionType.EXPENSE),
            PatternSpec(WITHDRAW_DATE_FIRST_REGEX, TransactionType.EXPENSE),
            PatternSpec(POCHI_REGEX, TransactionType.EXPENSE),
            PatternSpec(PAID_REGEX, TransactionType.EXPENSE),
            PatternSpec(SENT_REGEX, TransactionType.EXPENSE)
        )
    }
}
