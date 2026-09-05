package com.pesatrack.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// merchantKey is the normalized (trimmed, uppercased) merchant name -- see
// MerchantCategoryRepositoryImpl, the only place that reads/writes this table,
// for the normalization this key assumes.
@Entity(tableName = "merchant_category_map")
data class MerchantCategoryEntity(

    @PrimaryKey
    val merchantKey: String,

    val categoryId: Long
)
