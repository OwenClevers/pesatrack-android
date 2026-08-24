package com.pesatrack.app.di

import android.content.Context
import androidx.room.Room
import com.pesatrack.app.data.database.AppDatabase
import com.pesatrack.app.data.repository.TransactionRepositoryImpl
import com.pesatrack.app.domain.repository.TransactionRepository

object AppModule {

    private const val DATABASE_NAME = "pesatrack.db"

    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var repository: TransactionRepository? = null

    fun provideDatabase(context: Context): AppDatabase =
        database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            ).build().also { database = it }
        }

    fun provideTransactionRepository(context: Context): TransactionRepository =
        repository ?: synchronized(this) {
            repository ?: TransactionRepositoryImpl(
                provideDatabase(context).transactionDao()
            ).also { repository = it }
        }
}