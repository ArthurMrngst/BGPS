package com.example.bgps.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // Chỉ lấy giao dịch chưa bị xoá; Flow giúp UI tự cập nhật khi bảng thay đổi
    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    // Dùng để mở màn sửa giao dịch theo id lấy từ danh sách
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?

    // Upsert: chưa có id thì thêm, có rồi thì cập nhật -> dùng chung cho "thêm" và "sửa"
    @Upsert
    suspend fun upsert(transaction: TransactionEntity)

    // Xoá mềm: không xoá dòng, chỉ đánh dấu + đánh dấu cần đồng bộ
    @Query("UPDATE transactions SET isDeleted = 1, isDirty = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)

    // Dùng ở bước đồng bộ sau này: lấy các thay đổi chưa gửi lên server
    @Query("SELECT * FROM transactions WHERE isDirty = 1")
    suspend fun getDirty(): List<TransactionEntity>

    // Chống lưu trùng giao dịch từ thông báo (xem TransactionRepository.
    // findDuplicateNotificationTransaction): trùng mã tham chiếu (nếu có) HOẶC
    // cùng số tiền + loại trong một khoảng thời gian ngắn quanh thời điểm mới.
    @Query(
        """
        SELECT * FROM transactions
        WHERE isDeleted = 0 AND source = :source
        AND (
            (transactionRef IS NOT NULL AND transactionRef = :transactionRef)
            OR (amount = :amount AND type = :type AND timestamp BETWEEN :minTimestamp AND :maxTimestamp)
        )
        LIMIT 1
        """
    )
    suspend fun findPossibleDuplicate(
        source: TransactionSource,
        transactionRef: String?,
        amount: Long,
        type: TransactionType,
        minTimestamp: Long,
        maxTimestamp: Long
    ): TransactionEntity?

    // Dùng khi đổi tên hoặc xoá-gộp danh mục (B3 phần 2): mọi giao dịch đang
    // gắn danh mục cũ chuyển sang tên mới, đánh dấu isDirty như một lần sửa
    // bình thường để còn đồng bộ lên server sau này.
    @Query("UPDATE transactions SET category = :newCategory, isDirty = 1, updatedAt = :now WHERE category = :oldCategory")
    suspend fun renameCategory(oldCategory: String, newCategory: String, now: Long)
}