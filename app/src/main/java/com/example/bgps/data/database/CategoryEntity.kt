package com.example.bgps.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// Danh mục giờ là dữ liệu người dùng tự quản lý được (B3 phần 2), không còn
// là enum cố định — xem domain/DefaultCategories.kt cho 11 danh mục khởi tạo
// sẵn lúc cài app lần đầu. isBuiltIn chỉ để hiển thị gợi ý, không chặn sửa.
// "Khác" (FALLBACK) không được xoá — đích gộp mặc định khi xoá danh mục khác.
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val displayName: String,
    val icon: String? = null,
    val colorHex: String? = null,
    val isBuiltIn: Boolean = false,
    val sortOrder: Int = 0
)
