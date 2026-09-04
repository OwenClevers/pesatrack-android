package com.pesatrack.app.data.repository

import com.pesatrack.app.data.database.dao.BudgetDao
import com.pesatrack.app.data.mapper.toDomain
import com.pesatrack.app.data.mapper.toEntity
import com.pesatrack.app.domain.model.Budget
import com.pesatrack.app.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class BudgetRepositoryImpl(
    private val dao: BudgetDao
) : BudgetRepository {

    override fun getBudgets(month: YearMonth): Flow<List<Budget>> {
        return dao.getBudgetsForMonth(month)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun upsertBudget(budget: Budget) {
        dao.upsert(budget.toEntity())
    }
}
