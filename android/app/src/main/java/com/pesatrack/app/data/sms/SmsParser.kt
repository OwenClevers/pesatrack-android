package com.pesatrack.app.data.sms

import com.pesatrack.app.domain.model.TransactionType
import java.time.LocalDateTime

data class ParsedSmsTransaction(
    val transactionCode: String,
    val amount: Double,
    val counterparty: String,
    val timestamp: LocalDateTime,
    val type: TransactionType
)

/**
 * Parses confirmation SMS bodies from one sender into transactions.
 * [senderPattern] is what SmsReader queries the device inbox with, so a new
 * parser (a bank's shortcode, say -- a future milestone, not implemented
 * here) is registered by adding it to AppModule's parser list; neither
 * SmsReader nor the import flow that drives these parsers needs to change.
 */
interface SmsParser {

    val senderPattern: String

    fun parse(message: String): ParsedSmsTransaction?
}
