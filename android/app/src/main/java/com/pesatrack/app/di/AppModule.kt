package com.pesatrack.app.di

import android.content.Context
import androidx.room.Room
import com.pesatrack.app.data.backup.BackupManager
import com.pesatrack.app.data.database.AppDatabase
import com.pesatrack.app.data.database.MIGRATION_1_2
import com.pesatrack.app.data.database.MIGRATION_2_3
import com.pesatrack.app.data.database.MIGRATION_3_4
import com.pesatrack.app.data.database.categorySeedCallback
import com.pesatrack.app.data.repository.BudgetRepositoryImpl
import com.pesatrack.app.data.sms.MpesaSmsParser
import com.pesatrack.app.data.sms.SmsParser
import com.pesatrack.app.data.sms.SmsReader
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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

    fun provideSmsReader(context: Context): SmsReader =
        SmsReader(context.applicationContext)

    // Not cached as a singleton like the repositories above -- it's cheap to
    // build and only used from the Backup screen, so there's no shared state to
    // preserve across recompositions.
    fun provideBackupManager(context: Context): BackupManager =
        BackupManager(
            provideTransactionRepository(context),
            provideCategoryRepository(context),
            provideBudgetRepository(context)
        )

    // Registered SmsParser implementations, one per supported sender. A bank
    // parser is a future milestone -- adding it here (and nowhere else) is
    // meant to be the whole job once it exists.
    fun provideSmsParsers(): List<SmsParser> = listOf(MpesaSmsParser())
}
