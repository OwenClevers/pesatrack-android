package com.pesatrack.app.domain.repository

interface MerchantCategoryRepository {

    // Returns the learned category id for this merchant name, or null if
    // nothing has been learned for it yet. Matching is case- and
    // whitespace-insensitive.
    suspend fun getCategoryId(merchantName: String): Long?

    // Records (or overwrites) the category this merchant name should resolve
    // to next time -- called whenever a user assigns/changes a transaction's
    // category, so classification improves the more it gets corrected.
    suspend fun learn(merchantName: String, categoryId: Long)
}
