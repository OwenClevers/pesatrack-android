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

    // Used for SMS import: IGNORE so a row whose smsCode collides with an
    // existing one is skipped rather than overwritten. Returns -1 when ignored.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnoringDuplicates(transaction: TransactionEntity): Long

    @Query("""
        SELECT *
        FROM transactions
        ORDER BY transactionDate DESC
    """)
    fun getTransactions(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT *
        FROM transactions
        WHERE id = :id
    """)
    fun getTransactionById(id: Long): Flow<TransactionEntity?>

    @Query("""
        DELETE FROM transactions
        WHERE id = :id
    """)
    suspend fun deleteById(id: Long)

    @Query("""
        SELECT COUNT(*)
        FROM transactions
        WHERE categoryId = :categoryId
    """)
    suspend fun countByCategory(categoryId: Long): Int

    // Used for full-database restore: wipe then bulk-insert entities that already
    // carry their original ids, so transactions/budgets referencing those category
    // ids stay consistent without any remapping.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query("""
        DELETE FROM transactions
    """)
    suspend fun deleteAll()
}