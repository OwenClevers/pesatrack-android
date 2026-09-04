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

    val source: TransactionSource,

    // Null for a not-yet-persisted transaction; toEntity() then sets it to now()
    // on insert. Loaded transactions carry the original value through so an
    // update via toEntity() preserves it instead of overwriting it.
    val createdAt: LocalDateTime? = null
)