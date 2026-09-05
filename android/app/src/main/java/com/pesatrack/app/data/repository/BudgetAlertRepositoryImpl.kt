package com.pesatrack.app.data.repository

import com.pesatrack.app.data.database.dao.BudgetAlertDao
import com.pesatrack.app.data.database.entity.BudgetAlertEntity
import com.pesatrack.app.domain.model.BudgetThreshold
import com.pesatrack.app.domain.repository.BudgetAlertRepository
import java.time.YearMonth

class BudgetAlertRepositoryImpl(
    private val dao: BudgetAlertDao
) : BudgetAlertRepository {

    override suspend fun hasFired(categoryId: Long, month: YearMonth, threshold: BudgetThreshold): Boolean =
        dao.hasFired(categoryId, month, threshold.name)

    override suspend fun markFired(categoryId: Long, month: YearMonth, threshold: BudgetThreshold) {
        dao.markFired(BudgetAlertEntity(categoryId = categoryId, month = month, threshold = threshold.name))
    }
}
