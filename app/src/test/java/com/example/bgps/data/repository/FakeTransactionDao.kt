package com.example.bgps.data.repository

import com.example.bgps.data.database.TransactionDao
import com.example.bgps.data.database.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// Fake trong bộ nhớ thay cho Room, dùng cho unit test (JVM thuần, không cần thiết bị).
// Chỉ mô phỏng đúng hợp đồng của các @Query trong TransactionDao (lọc isDeleted,
// đánh dấu isDirty khi xoá mềm) để test Repository mà không cần SQLite thật.
class FakeTransactionDao : TransactionDao {
    private val state = MutableStateFlow<List<TransactionEntity>>(emptyList())

    override fun observeAll(): Flow<List<TransactionEntity>> =
        state.map { list -> list.filterNot { it.isDeleted }.sortedByDescending { it.timestamp } }

    override suspend fun getById(id: String): TransactionEntity? =
        state.value.find { it.id == id }

    override suspend fun upsert(transaction: TransactionEntity) {
        state.value = state.value.filterNot { it.id == transaction.id } + transaction
    }

    override suspend fun softDelete(id: String, now: Long) {
        state.value = state.value.map { tx ->
            if (tx.id == id) tx.copy(isDeleted = true, isDirty = true, updatedAt = now) else tx
        }
    }

    override suspend fun getDirty(): List<TransactionEntity> =
        state.value.filter { it.isDirty }
}
