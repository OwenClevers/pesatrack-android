package com.pesatrack.app.domain.repository

import com.pesatrack.app.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getTransactions(): Flow<List<Transaction>>

    fun getTransaction(id: Long): Flow<Transaction?>

    suspend fun addTransaction(transaction: Transaction)

    suspend fun deleteTransaction(id: Long)
}