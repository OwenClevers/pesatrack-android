package com.pesatrack.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: `TransactionEntity.kt`)

    @Query("""
        SELECT *
        FROM transactions
        ORDER BY transactionDate DESC
    """)
    fun getTransactions(): Flow<List<`TransactionEntity.kt`>>

    @Query("""
        SELECT *
        FROM transactions
        LIMIT 1
    """)
    suspend fun getLatestTransaction(): `TransactionEntity.kt`?

    @Query("""
        DELETE FROM transactions
    """)
    suspend fun deleteAll()
}