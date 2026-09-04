package com.pesatrack.app.domain.model

import java.time.YearMonth

data class Budget(
    val id: Long,
    val categoryId: Long,
    val limit: Double,
    val month: YearMonth
)
