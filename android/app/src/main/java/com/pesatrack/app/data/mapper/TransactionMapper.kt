package com.pesatrack.app.data.mapper

import com.pesatrack.app.data.database.entity.TransactionEntity
import com.pesatrack.app.domain.model.Transaction
import java.time.LocalDateTime

fun TransactionEntity.toDomain(): Transaction =
    Transaction(
        id = id,
        amount = amount,
        type = type,
        categoryId = categoryId,
        merchant = merchant,
        description = description,
        transactionDate = transactionDate,
        source = source,
        createdAt = createdAt,
        smsCode = smsCode
    )

// createdAt is null on a not-yet-persisted Transaction (new row -> now()) and
// carries the original value on one loaded from the DB (update -> preserved).
fun Transaction.toEntity(): TransactionEntity {
    val now = LocalDateTime.now()
    return TransactionEntity(
        id = id,
        amount = amount,
        type = type,
        categoryId = categoryId,
        merchant = merchant,
        description = description,
        transactionDate = transactionDate,
        source = source,
        createdAt = createdAt ?: now,
        updatedAt = now,
        smsCode = smsCode
    )
}