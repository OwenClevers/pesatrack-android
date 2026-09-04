package com.pesatrack.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
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
        SET name = :name
        WHERE id = :id
    """)
    suspend fun updateName(id: Long, name: String)

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
}
