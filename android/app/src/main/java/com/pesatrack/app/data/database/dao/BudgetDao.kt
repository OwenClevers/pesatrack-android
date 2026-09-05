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

    // Used for backup export -- a one-shot snapshot across every month, unlike
    // getBudgetsForMonth's reactive per-month query.
    @Query("""
        SELECT *
        FROM budgets
        ORDER BY month DESC, categoryId ASC
    """)
    suspend fun getAllBudgets(): List<BudgetEntity>

    // REPLACE also resolves the unique (categoryId, month) index, so saving a
    // budget for a category/month that's already set updates that row in place
    // instead of violating the constraint.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)

    @Query("""
        DELETE FROM budgets
        WHERE id = :id
    """)
    suspend fun deleteById(id: Long)

    // Used for full-database restore: wipe then bulk-insert entities that already
    // carry their original ids, matching TransactionDao/CategoryDao.insertAll.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Query("""
        DELETE FROM budgets
    """)
    suspend fun deleteAll()
}
