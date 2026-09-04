package com.pesatrack.app.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.pesatrack.app.data.database.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

@Dao
interface BudgetDao {

    @Query("""
        SELECT *
        FROM budgets
        WHERE month = :month
        ORDER BY categoryId ASC
    """)
    fun getBudgetsForMonth(month: YearMonth): Flow<List<BudgetEntity>>
}
