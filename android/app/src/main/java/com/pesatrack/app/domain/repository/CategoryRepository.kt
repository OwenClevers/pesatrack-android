package com.pesatrack.app.domain.repository

import com.pesatrack.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getCategories(): Flow<List<Category>>
}
