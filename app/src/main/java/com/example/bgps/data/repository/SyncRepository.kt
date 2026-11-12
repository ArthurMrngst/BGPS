package com.example.bgps.data.repository

import android.content.Context
import com.example.bgps.data.database.TransactionDao
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionSource
import com.example.bgps.data.database.TransactionType
import com.example.bgps.data.network.ClientTransactionDto
import com.example.bgps.data.network.SyncApi
import com.example.bgps.data.network.SyncPushRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Đồng bộ TUỲ CHỌN với backend — chỉ gọi khi người dùng đã đăng nhập và tự
// bấm "Đồng bộ ngay" (AccountScreen), không chạy nền tự động. Trip/Budget/
// Category CHƯA đồng bộ, mới làm phần giao dịch.
@Singleton
class SyncRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val transactionDao: TransactionDao,
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("sync_state", Context.MODE_PRIVATE)

    suspend fun sync() {
        pushDirty()
        pullChanges()
    }

    private suspend fun pushDirty() {
        val dirty = transactionDao.getDirty()
        if (dirty.isEmpty()) return

        syncApi.push(SyncPushRequest(dirty.map { it.toDto() }))
        dirty.forEach { transactionDao.clearDirty(it.id) }
    }

    private suspend fun pullChanges() {
        val since = prefs.getLong(KEY_LAST_SYNC, 0L)
        val changes = syncApi.pull(since)
        if (changes.isEmpty()) return

        changes.forEach { dto -> transactionDao.upsert(dto.toEntity()) }
        val newest = changes.maxOf { it.updatedAt }
        prefs.edit().putLong(KEY_LAST_SYNC, newest).apply()
    }

    companion object {
        private const val KEY_LAST_SYNC = "last_sync_at"
    }
}

private fun TransactionEntity.toDto() = ClientTransactionDto(
    id = id,
    amount = amount,
    type = type.name,
    category = category,
    merchant = merchant,
    note = note,
    source = source.name,
    timestamp = timestamp,
    latitude = latitude,
    longitude = longitude,
    transactionRef = transactionRef,
    updatedAt = updatedAt,
    isDeleted = isDeleted
)

// isDirty = false: dữ liệu vừa kéo từ server về, không có gì cần gửi lại.
private fun ClientTransactionDto.toEntity() = TransactionEntity(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type),
    category = category,
    merchant = merchant,
    note = note,
    source = TransactionSource.valueOf(source),
    timestamp = timestamp,
    latitude = latitude,
    longitude = longitude,
    transactionRef = transactionRef,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    isDirty = false
)
