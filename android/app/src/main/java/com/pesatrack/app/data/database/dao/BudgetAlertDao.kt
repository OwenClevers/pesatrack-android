package com.pesatrack.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pesatrack.app.data.database.entity.BudgetAlertEntity
import java.time.YearMonth

@Dao
interface BudgetAlertDao {

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM budget_alerts
            WHERE categoryId = :categoryId AND month = :month AND threshold = :threshold
        )
        """
    )
    suspend fun hasFired(categoryId: Long, month: YearMonth, threshold: String): Boolean

    // IGNORE on the unique (categoryId, month, threshold) index -- marking an
    // already-fired alert fired again is a no-op, not an error.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markFired(alert: BudgetAlertEntity)
}
