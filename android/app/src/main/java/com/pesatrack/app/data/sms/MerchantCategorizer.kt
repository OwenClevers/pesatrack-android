package com.pesatrack.app.data.sms

import com.pesatrack.app.domain.model.Category
import com.pesatrack.app.domain.repository.MerchantCategoryRepository

/**
 * Resolves the category a newly-imported (or manually entered) transaction
 * should get, keyed off its counterparty/merchant name. Checks what the user
 * has already taught it via [MerchantCategoryRepository] before falling back
 * to a keyword guess, so classification gets better the more a user corrects
 * it instead of staying static.
 */
class MerchantCategorizer(
    private val merchantCategoryRepository: MerchantCategoryRepository
) {

    suspend fun classify(counterparty: String, categories: List<Category>): Long {
        val learnedCategoryId = merchantCategoryRepository.getCategoryId(counterparty)
        if (learnedCategoryId != null && categories.any { it.id == learnedCategoryId }) {
            return learnedCategoryId
        }

        val iconKey = bestKeywordMatch(counterparty) ?: DEFAULT_ICON_KEY
        return categories.firstOrNull { it.iconKey == iconKey }?.id
            ?: categories.firstOrNull { it.iconKey == DEFAULT_ICON_KEY }?.id
            ?: categories.firstOrNull()?.id
            ?: 0L
    }

    // Scores every keyword that appears in the text by length, so a longer,
    // more specific keyword (e.g. "NAIVAS") beats a shorter, generic one
    // (e.g. "FUEL") that also happens to appear -- rather than whichever
    // category's keyword list is declared first winning by accident. Fixes
    // "NAIVAS FUEL STATION" resolving to fuel purely because fuel's list is
    // checked before shopping's.
    private fun bestKeywordMatch(counterparty: String): String? {
        val text = counterparty.uppercase()
        return categoryKeywords.entries
            .flatMap { (iconKey, keywords) -> keywords.filter { text.contains(it) }.map { iconKey to it } }
            .maxByOrNull { (_, keyword) -> keyword.length }
            ?.first
    }

    private companion object {

        const val DEFAULT_ICON_KEY = "other"

        // Keyed by the seeded categories' iconKey. "other" is a normal (if
        // trivial) keyword bucket here like any other -- for fixed literal
        // names the parser itself produces that don't belong to a spending
        // category -- not a special case; a name matching none of these
        // keywords at all still falls back to DEFAULT_ICON_KEY above.
        val categoryKeywords: Map<String, List<String>> = mapOf(
            "food" to listOf("RESTAURANT", "CAFE", "HOTEL", "EATERY", "KFC", "JAVA", "PIZZA", "CHICKEN", "BAKERY"),
            "fuel" to listOf("PETROL", "FUEL", "SHELL", "TOTAL ENERGIES", "OILIBYA", "RUBIS", "GAS STATION"),
            "shopping" to listOf(
                "SUPERMARKET", "MART", "SHOP", "STORE", "NAIVAS", "CARREFOUR", "QUICKMART", "TUSKYS",
                // Pochi la Biashara -- a small-business till, closest fit
                // among the seeded categories.
                "POCHI"
            ),
            "utilities" to listOf(
                "KPLC", "ELECTRICITY", "WATER", "UTILITY", "UTILITIES",
                // Airtime/data bundles -- a recurring utility-like spend.
                "AIRTIME"
            ),
            "entertainment" to listOf("CINEMA", "MOVIE", "NETFLIX", "SHOWMAX", "ENTERTAINMENT"),
            "transport" to listOf("UBER", "BOLT", "TAXI", "MATATU", "TRANSPORT", "BUS"),
            "medical" to listOf("HOSPITAL", "CLINIC", "PHARMACY", "CHEMIST", "MEDICAL"),
            "education" to listOf("SCHOOL", "UNIVERSITY", "COLLEGE", "TUITION", "EDUCATION"),
            "other" to listOf(
                // Fuliza is M-PESA's overdraft product, not a spending category.
                "FULIZA"
            )
        )
    }
}
