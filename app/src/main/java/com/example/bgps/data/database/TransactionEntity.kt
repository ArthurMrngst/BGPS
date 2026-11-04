package com.example.bgps.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class TransactionType { EXPENSE, INCOME }

// Nguồn ghi nhận: notification/OCR/GPS/nhập tay đều ghi vào cùng bảng này
enum class TransactionSource { MANUAL, NOTIFICATION, OCR, GPS }

@Entity(tableName = "transactions")
data class TransactionEntity(
    // UUID do app tự tạo: hai máy tạo giao dịch cùng lúc sẽ không bao giờ trùng id
    // (id tự tăng 1, 2, 3 thì hai máy sẽ đụng nhau khi đồng bộ).
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val amount: Long,                 // VND không có số lẻ nên dùng Long
    val type: TransactionType,
    val category: String,
    val merchant: String? = null,
    val note: String? = null,
    val source: TransactionSource = TransactionSource.MANUAL,
    val timestamp: Long,              // thời điểm xảy ra giao dịch (epoch millis)
    val latitude: Double? = null,
    val longitude: Double? = null,

    // Mã tham chiếu giao dịch đọc được từ thông báo ngân hàng (nếu có) — dùng
    // để chống lưu trùng khi cùng một thông báo được bắn 2 lần. Hiện chưa có
    // parser nào trích xuất được mã thật (mẫu thật bắt được của Liobank/
    // Techcombank chỉ có số tài khoản, không có mã giao dịch riêng), nên cột
    // này gần như luôn null — khi đó repository tự dùng heuristic dự phòng
    // (cùng nguồn + số tiền + loại + trong khoảng thời gian ngắn) thay thế.
    val transactionRef: String? = null,

    // --- Các cột phục vụ đồng bộ ---
    val updatedAt: Long = System.currentTimeMillis(), // lần sửa gần nhất, để biết bản nào mới hơn
    val isDeleted: Boolean = false,   // xoá mềm: giữ dòng lại để máy khác biết giao dịch đã bị xoá
    val isDirty: Boolean = true       // true = có thay đổi chưa gửi lên server
)