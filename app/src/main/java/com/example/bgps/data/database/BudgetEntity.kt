package com.example.bgps.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Ngân sách hàng tháng theo danh mục — category khớp với TransactionEntity.
// category (chuỗi hiển thị từ domain.Category). Một danh mục chỉ có một
// ngân sách (category là khoá chính); chưa đặt thì không có dòng nào, nghĩa
// là danh mục đó không bị theo dõi ngân sách.
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val category: String,
    val monthlyLimit: Long
)
