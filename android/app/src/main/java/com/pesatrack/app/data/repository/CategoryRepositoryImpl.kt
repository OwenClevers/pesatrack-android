package com.pesatrack.app.data.repository

import com.pesatrack.app.data.database.dao.CategoryDao
import com.pesatrack.app.data.database.dao.TransactionDao
import com.pesatrack.app.data.database.entity.CategoryEntity
import com.pesatrack.app.data.mapper.toDomain
import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.model.CategoryDeleteResult
import com.pesatrack.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val dao: CategoryDao,
    private val transactionDao: TransactionDao
) : CategoryRepository {

    override fun getCategories(): Flow<List<Category>> {
        return dao.getCategories()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun addCategory(name: String, iconKey: String, colorKey: String): Category {
        val id = (dao.getMaxId() ?: 0) + 1
        val entity = CategoryEntity(id = id, name = name, iconKey = iconKey, colorKey = colorKey)
        dao.insert(entity)
        return entity.toDomain()
    }

    override suspend fun updateCategory(id: Long, name: String, iconKey: String, colorKey: String) {
        dao.updateCategory(id, name, iconKey, colorKey)
    }

    override suspend fun deleteCategory(id: Long): CategoryDeleteResult {
        val transactionCount = transactionDao.countByCategory(id)
        if (transactionCount > 0) {
            return CategoryDeleteResult.Blocked(transactionCount)
        }
        dao.deleteById(id)
        return CategoryDeleteResult.Deleted
    }
}
