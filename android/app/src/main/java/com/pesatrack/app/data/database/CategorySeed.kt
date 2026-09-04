package com.pesatrack.app.data.database

import androidx.sqlite.db.SupportSQLiteDatabase

internal data class SeedCategory(
    val id: Long,
    val name: String,
    val iconKey: String,
    val colorKey: String
)

// Mirrors the ids of the old Category enum so existing categoryId values on
// persisted transactions keep resolving to the same category after migration.
internal val categorySeed = listOf(
    SeedCategory(1, "Food", "food", "food"),
    SeedCategory(2, "Fuel", "fuel", "fuel"),
    SeedCategory(3, "Shopping", "shopping", "shopping"),
    SeedCategory(4, "Utilities", "utilities", "utilities"),
    SeedCategory(5, "Entertainment", "entertainment", "entertainment"),
    SeedCategory(6, "Transport", "transport", "transport"),
    SeedCategory(7, "Medical", "medical", "medical"),
    SeedCategory(8, "Education", "education", "education"),
    SeedCategory(9, "Other", "other", "other")
)

internal fun insertCategorySeed(db: SupportSQLiteDatabase) {
    categorySeed.forEach { category ->
        db.execSQL(
            "INSERT OR IGNORE INTO categories (id, name, iconKey, colorKey) VALUES (?, ?, ?, ?)",
            arrayOf<Any>(category.id, category.name, category.iconKey, category.colorKey)
        )
    }
}
