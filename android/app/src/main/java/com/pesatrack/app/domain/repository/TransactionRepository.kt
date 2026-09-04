package com.pesatrack.app.domain.repository

import com.pesatrack.app.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getTransactions(): Flow<List<Transaction>>

    fun getTransaction(id: Long): Flow<Transaction?>

    suspend fun addTransaction(transaction: Transaction)

    suspend fun deleteTransaction(id: Long)

    // Returns true if the transaction was newly inserted, false if smsCode
    // already exists (i.e. it's a duplicate that was skipped).
    suspend fun importMpesaTransaction(transaction: Transaction, smsCode: String): Boolean
}