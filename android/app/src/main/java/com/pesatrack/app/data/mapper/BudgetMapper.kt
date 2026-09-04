package com.pesatrack.app.data.mapper

import com.pesatrack.app.data.database.entity.BudgetEntity
import com.pesatrack.app.domain.model.Budget

fun BudgetEntity.toDomain(): Budget =
    Budget(
        id = id,
        categoryId = categoryId,
        limit = limit,
        month = month
    )

fun Budget.toEntity(): BudgetEntity =
    BudgetEntity(
        id = id,
        categoryId = categoryId,
        limit = limit,
        month = month
    )
