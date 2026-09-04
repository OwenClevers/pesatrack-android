package com.pesatrack.app.data.mapper

import com.pesatrack.app.data.database.entity.CategoryEntity
import com.pesatrack.app.domain.model.Category

fun CategoryEntity.toDomain(): Category =
    Category(
        id = id,
        name = name,
        iconKey = iconKey,
        colorKey = colorKey
    )

fun Category.toEntity(): CategoryEntity =
    CategoryEntity(
        id = id,
        name = name,
        iconKey = iconKey,
        colorKey = colorKey
    )
