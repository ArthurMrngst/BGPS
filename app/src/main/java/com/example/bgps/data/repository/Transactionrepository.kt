package com.example.bgps.data.repository

import com.example.bgps.data.database.TransactionDao
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionSource
import com.example.bgps.data.database.TransactionType
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

    // Dùng trước khi lưu giao dịch đọc từ thông báo ngân hàng, để không lưu
    // trùng khi cùng một thông báo được hệ thống bắn/đăng lại 2 lần (xem
    // TransactionDao.findPossibleDuplicate + ghi chú ở TransactionEntity.
    // transactionRef về việc parser hiện chưa trích được mã thật).
    suspend fun findDuplicateNotificationTransaction(
        transactionRef: String?,
        amount: Long,
        type: TransactionType,
        timestamp: Long
    ): TransactionEntity? = dao.findPossibleDuplicate(
        source = TransactionSource.NOTIFICATION,
        transactionRef = transactionRef,
        amount = amount,
        type = type,
        minTimestamp = timestamp - DUPLICATE_WINDOW_MILLIS,
        maxTimestamp = timestamp + DUPLICATE_WINDOW_MILLIS
    )

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

    companion object {
        private const val DUPLICATE_WINDOW_MILLIS = 2 * 60 * 1000L
    }
}