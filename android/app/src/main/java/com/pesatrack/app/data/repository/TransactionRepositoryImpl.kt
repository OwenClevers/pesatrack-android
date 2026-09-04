package com.pesatrack.app.data.repository

import com.pesatrack.app.data.database.dao.TransactionDao
import com.pesatrack.app.data.mapper.toDomain
import com.pesatrack.app.data.mapper.toEntity
import com.pesatrack.app.domain.model.Transaction
import com.pesatrack.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getTransactions(): Flow<List<Transaction>> {
        return dao.getTransactions()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override fun getTransaction(id: Long): Flow<Transaction?> {
        return dao.getTransactionById(id)
            .map { entity -> entity?.toDomain() }
    }

    override suspend fun addTransaction(
        transaction: Transaction
    ) {
        dao.insert(
            transaction.toEntity()
        )
    }

    override suspend fun deleteTransaction(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun importMpesaTransaction(transaction: Transaction, smsCode: String): Boolean {
        val rowId = dao.insertIgnoringDuplicates(transaction.toEntity().copy(smsCode = smsCode))
        return rowId != -1L
    }
}