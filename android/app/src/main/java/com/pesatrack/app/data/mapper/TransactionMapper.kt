package com.pesatrack.app.data.mapper

import com.pesatrack.app.data.database.entity.TransactionEntity
import com.pesatrack.app.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction =
    Transaction(
        id = id,
        amount = amount,
        type = type,
        categoryId = categoryId,
        merchant = merchant,
        description = description,
        transactionDate = transactionDate,
        source = source
    )

fun Transaction.toEntity(): TransactionEntity =
    TransactionEntity(
        id = id,
        amount = amount,
        type = type,
        categoryId = categoryId,
        merchant = merchant,
        description = description,
        transactionDate = transactionDate,
        source = source,
        createdAt = transactionDate,
        updatedAt = transactionDate
    )