package com.pesatrack.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.pesatrack.app.data.database.entity.TransactionEntity

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Query("""
        SELECT *
        FROM transactions
        ORDER BY transactionDate DESC
    """)
    fun getTransactions(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT *
        FROM transactions
        ORDER BY transactionDate DESC
        LIMIT 1
    """)
    suspend fun getLatestTransaction(): TransactionEntity?

    @Query("""
        DELETE FROM transactions
    """)
    suspend fun deleteAll()
}