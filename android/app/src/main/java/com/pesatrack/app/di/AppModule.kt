package com.pesatrack.app.di

import android.content.Context
import androidx.room.Room
import com.pesatrack.app.data.database.AppDatabase
import com.pesatrack.app.data.database.MIGRATION_1_2
import com.pesatrack.app.data.database.MIGRATION_2_3
import com.pesatrack.app.data.database.categorySeedCallback
import com.pesatrack.app.data.repository.BudgetRepositoryImpl
import com.pesatrack.app.data.repository.CategoryRepositoryImpl
import com.pesatrack.app.data.repository.TransactionRepositoryImpl
import com.pesatrack.app.domain.repository.BudgetRepository
import com.pesatrack.app.domain.repository.CategoryRepository
import com.pesatrack.app.domain.repository.TransactionRepository

object AppModule {

    private const val DATABASE_NAME = "pesatrack.db"

    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var repository: TransactionRepository? = null

    @Volatile
    private var categoryRepository: CategoryRepository? = null

    @Volatile
    private var budgetRepository: BudgetRepository? = null

    fun provideDatabase(context: Context): AppDatabase =
        database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(categorySeedCallback)
                .build()
                .also { database = it }
        }

    fun provideTransactionRepository(context: Context): TransactionRepository =
        repository ?: synchronized(this) {
            repository ?: TransactionRepositoryImpl(
                provideDatabase(context).transactionDao()
            ).also { repository = it }
        }

    fun provideCategoryRepository(context: Context): CategoryRepository =
        categoryRepository ?: synchronized(this) {
            categoryRepository ?: CategoryRepositoryImpl(
                provideDatabase(context).categoryDao(),
                provideDatabase(context).transactionDao()
            ).also { categoryRepository = it }
        }

    fun provideBudgetRepository(context: Context): BudgetRepository =
        budgetRepository ?: synchronized(this) {
            budgetRepository ?: BudgetRepositoryImpl(
                provideDatabase(context).budgetDao()
            ).also { budgetRepository = it }
        }
}
