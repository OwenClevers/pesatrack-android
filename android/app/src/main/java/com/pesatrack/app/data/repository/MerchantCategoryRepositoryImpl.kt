package com.pesatrack.app.data.repository

import com.pesatrack.app.data.database.dao.MerchantCategoryDao
import com.pesatrack.app.data.database.entity.MerchantCategoryEntity
import com.pesatrack.app.domain.repository.MerchantCategoryRepository

class MerchantCategoryRepositoryImpl(
    private val dao: MerchantCategoryDao
) : MerchantCategoryRepository {

    override suspend fun getCategoryId(merchantName: String): Long? {
        val key = normalize(merchantName) ?: return null
        return dao.getCategoryId(key)
    }

    override suspend fun learn(merchantName: String, categoryId: Long) {
        val key = normalize(merchantName) ?: return
        dao.upsert(MerchantCategoryEntity(merchantKey = key, categoryId = categoryId))
    }

    private fun normalize(merchantName: String): String? {
        val trimmed = merchantName.trim()
        return if (trimmed.isEmpty()) null else trimmed.uppercase()
    }
}
