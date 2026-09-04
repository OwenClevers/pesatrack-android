package com.pesatrack.app.domain.repository

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.CategoryDeleteResult
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getCategories(): Flow<List<Category>>

    suspend fun addCategory(name: String): Category

    suspend fun renameCategory(id: Long, name: String)

    suspend fun deleteCategory(id: Long): CategoryDeleteResult
}
