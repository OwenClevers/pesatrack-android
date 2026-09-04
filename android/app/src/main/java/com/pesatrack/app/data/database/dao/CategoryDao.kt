package com.pesatrack.app.data.database.dao

import androidx.room.Dao
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
}
