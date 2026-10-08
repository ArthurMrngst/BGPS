

package com.example.bgps.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType { EXPENSE, INCOME }

// Nguồn ghi nhận: sau này notification/OCR/GPS đều ghi vào cùng bảng này
enum class TransactionSource { MANUAL, NOTIFICATION, OCR, GPS }

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,                 // VND không có số lẻ nên dùng Long, tránh sai số của Double
    val type: TransactionType,
    val category: String,
    val merchant: String? = null,
    val note: String? = null,
    val source: TransactionSource = TransactionSource.MANUAL,
    val timestamp: Long,              // epoch millis
    val latitude: Double? = null,     // dùng cho bản đồ chi tiêu ở milestone sau
    val longitude: Double? = null
)
