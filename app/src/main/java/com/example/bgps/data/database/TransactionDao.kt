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

    // Upsert: chưa có id thì thêm, có rồi thì cập nhật -> dùng chung cho "thêm" và "sửa"
    @Upsert
    suspend fun upsert(transaction: TransactionEntity)

    // Xoá mềm: không xoá dòng, chỉ đánh dấu + đánh dấu cần đồng bộ
    @Query("UPDATE transactions SET isDeleted = 1, isDirty = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)

    // Dùng ở bước đồng bộ sau này: lấy các thay đổi chưa gửi lên server
    @Query("SELECT * FROM transactions WHERE isDirty = 1")
    suspend fun getDirty(): List<TransactionEntity>
}