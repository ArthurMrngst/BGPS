package com.example.bgps.data.repository

import com.example.bgps.data.database.TransactionDao
import com.example.bgps.data.database.TransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// Mọi nguồn dữ liệu (nhập tay, notification, OCR, GPS) đều đi qua class này
@Singleton
class TransactionRepository @Inject constructor(
    private val dao: TransactionDao
) {
    fun observeTransactions(): Flow<List<TransactionEntity>> = dao.observeAll()

    suspend fun add(transaction: TransactionEntity): Long = dao.insert(transaction)

    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)
}
