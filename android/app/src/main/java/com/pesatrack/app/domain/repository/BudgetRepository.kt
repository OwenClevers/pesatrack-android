package com.pesatrack.app.domain.repository

import com.pesatrack.app.domain.model.Budget
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface BudgetRepository {

    fun getBudgets(month: YearMonth): Flow<List<Budget>>

    // One-shot snapshot across every month, for backup export.
    suspend fun getAllBudgets(): List<Budget>

    suspend fun upsertBudget(budget: Budget)

    suspend fun deleteBudget(id: Long)

    // Wipes every budget and inserts these in its place, preserving their ids
    // as-is. Used for a "replace" backup restore.
    suspend fun replaceAll(budgets: List<Budget>)
}
