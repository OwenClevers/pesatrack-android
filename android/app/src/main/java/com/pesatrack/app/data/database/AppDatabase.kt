package com.pesatrack.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pesatrack.app.data.database.converters.Converters
import com.pesatrack.app.data.database.dao.BudgetDao
import com.pesatrack.app.data.database.dao.CategoryDao
import com.pesatrack.app.data.database.dao.TransactionDao
import com.pesatrack.app.data.database.entity.BudgetEntity
import com.pesatrack.app.data.database.entity.CategoryEntity
import com.pesatrack.app.data.database.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, BudgetEntity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun categoryDao(): CategoryDao

    abstract fun budgetDao(): BudgetDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `categories` (
                `id` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `iconKey` TEXT NOT NULL,
                `colorKey` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        insertCategorySeed(db)
    }
}

val categorySeedCallback = object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        insertCategorySeed(db)
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `budgets` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `categoryId` INTEGER NOT NULL,
                `limit` REAL NOT NULL,
                `month` TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS `index_budgets_categoryId_month`
            ON `budgets` (`categoryId`, `month`)
            """.trimIndent()
        )
    }
}