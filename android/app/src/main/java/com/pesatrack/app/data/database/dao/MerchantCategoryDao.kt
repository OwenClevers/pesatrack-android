package com.pesatrack.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pesatrack.app.data.database.entity.MerchantCategoryEntity

@Dao
interface MerchantCategoryDao {

    @Query("""
        SELECT categoryId
        FROM merchant_category_map
        WHERE merchantKey = :merchantKey
    """)
    suspend fun getCategoryId(merchantKey: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(mapping: MerchantCategoryEntity)
}
