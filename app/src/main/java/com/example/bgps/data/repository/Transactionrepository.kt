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

    suspend fun getById(id: String): TransactionEntity? = dao.getById(id)

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

    // Sửa giao dịch đã có: giữ nguyên id, chỉ cập nhật nội dung + mốc thời gian/isDirty.
    suspend fun update(transaction: TransactionEntity) = save(transaction)

    suspend fun delete(transaction: TransactionEntity) {
        dao.softDelete(transaction.id, System.currentTimeMillis())
    }
}