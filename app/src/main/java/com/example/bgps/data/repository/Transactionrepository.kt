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

    // Dùng cho cả thêm mới và sửa. Repository tự gắn updatedAt/isDirty
    // để phía UI không thể quên.
    suspend fun save(transaction: TransactionEntity) {
        dao.upsert(
            transaction.copy(
                updatedAt = System.currentTimeMillis(),
                isDirty = true
            )
        )
    }

    suspend fun delete(transaction: TransactionEntity) {
        dao.softDelete(transaction.id, System.currentTimeMillis())
    }
}