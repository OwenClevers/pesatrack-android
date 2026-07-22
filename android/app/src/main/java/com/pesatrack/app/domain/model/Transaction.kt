package com.pesatrack.app.domain.model

import java.time.LocalDateTime

data class Transaction(

    val id: Long,

    val amount: Double,

    val type: TransactionType,

    val categoryId: Long,

    val merchant: String?,

    val description: String?,

    val transactionDate: LocalDateTime,

    val source: TransactionSource
)