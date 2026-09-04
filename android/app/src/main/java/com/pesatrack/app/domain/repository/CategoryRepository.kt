package com.pesatrack.app.domain.repository

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.CategoryDeleteResult
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getCategories(): Flow<List<Category>>

    suspend fun addCategory(name: String, iconKey: String, colorKey: String): Category

    suspend fun updateCategory(id: Long, name: String, iconKey: String, colorKey: String)

    suspend fun deleteCategory(id: Long): CategoryDeleteResult
}
