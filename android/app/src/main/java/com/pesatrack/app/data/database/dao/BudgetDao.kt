package com.pesatrack.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    // REPLACE also resolves the unique (categoryId, month) index, so saving a
    // budget for a category/month that's already set updates that row in place
    // instead of violating the constraint.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)
}
