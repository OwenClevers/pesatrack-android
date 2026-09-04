package com.pesatrack.app.data.repository

import com.pesatrack.app.data.database.dao.CategoryDao
import com.pesatrack.app.data.mapper.toDomain
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val dao: CategoryDao
) : CategoryRepository {

    override fun getCategories(): Flow<List<Category>> {
        return dao.getCategories()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }
}
