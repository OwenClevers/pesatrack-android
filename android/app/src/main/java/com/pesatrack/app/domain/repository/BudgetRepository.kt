package com.pesatrack.app.domain.repository

import com.pesatrack.app.domain.model.Budget
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface BudgetRepository {

    fun getBudgets(month: YearMonth): Flow<List<Budget>>

    suspend fun upsertBudget(budget: Budget)

    suspend fun deleteBudget(id: Long)
}
