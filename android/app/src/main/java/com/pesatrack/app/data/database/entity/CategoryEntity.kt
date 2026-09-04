package com.pesatrack.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(

    @PrimaryKey
    val id: Long,

    val name: String,

    val iconKey: String,

    val colorKey: String
)
