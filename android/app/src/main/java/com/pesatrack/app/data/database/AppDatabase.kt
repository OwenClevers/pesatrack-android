package com.pesatrack.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pesatrack.app.data.database.converters.Converters
import com.pesatrack.app.data.database.dao.BudgetAlertDao
import com.pesatrack.app.data.database.dao.BudgetDao
import com.pesatrack.app.data.database.dao.CategoryDao
import com.pesatrack.app.data.database.dao.MerchantCategoryDao
import com.pesatrack.app.data.database.dao.TransactionDao
import com.pesatrack.app.data.database.entity.BudgetAlertEntity
import com.pesatrack.app.data.database.entity.BudgetEntity
import com.pesatrack.app.data.database.entity.CategoryEntity
import com.pesatrack.app.data.database.entity.MerchantCategoryEntity
import com.pesatrack.app.data.database.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        MerchantCategoryEntity::class,
        BudgetAlertEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun categoryDao(): CategoryDao

    abstract fun budgetDao(): BudgetDao

    abstract fun merchantCategoryDao(): MerchantCategoryDao

    abstract fun budgetAlertDao(): BudgetAlertDao
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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE `transactions` ADD COLUMN `smsCode` TEXT DEFAULT NULL
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS `index_transactions_smsCode`
            ON `transactions` (`smsCode`)
            """.trimIndent()
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `merchant_category_map` (
                `merchantKey` TEXT NOT NULL,
                `categoryId` INTEGER NOT NULL,
                PRIMARY KEY(`merchantKey`)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `budget_alerts` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `categoryId` INTEGER NOT NULL,
                `month` TEXT NOT NULL,
                `threshold` TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS `index_budget_alerts_categoryId_month_threshold`
            ON `budget_alerts` (`categoryId`, `month`, `threshold`)
            """.trimIndent()
        )
    }
}