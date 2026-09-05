package com.pesatrack.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pesatrack.app.data.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("""
        SELECT *
        FROM categories
        ORDER BY id ASC
    """)
    fun getCategories(): Flow<List<CategoryEntity>>

    @Insert
    suspend fun insert(category: CategoryEntity)

    @Query("""
        UPDATE categories
        SET name = :name, iconKey = :iconKey, colorKey = :colorKey
        WHERE id = :id
    """)
    suspend fun updateCategory(id: Long, name: String, iconKey: String, colorKey: String)

    @Query("""
        DELETE FROM categories
        WHERE id = :id
    """)
    suspend fun deleteById(id: Long)

    @Query("""
        SELECT MAX(id)
        FROM categories
    """)
    suspend fun getMaxId(): Long?

    // Used for full-database restore: wipe then bulk-insert entities that already
    // carry their original ids, matching TransactionDao/BudgetDao.insertAll.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("""
        DELETE FROM categories
    """)
    suspend fun deleteAll()
}
