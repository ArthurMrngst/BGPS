package com.example.bgps.domain

import com.example.bgps.data.database.CategoryEntity

// 11 danh mục khởi tạo sẵn lúc cài app lần đầu (trước đây là enum Category
// cố định, giờ chuyển thành dữ liệu để người dùng tự thêm/sửa/xoá được — xem
// CategoryRepository). FALLBACK_NAME là đích gộp mặc định khi xoá một danh
// mục khác, và không được phép tự xoá.
object DefaultCategories {
    const val FALLBACK_NAME = "Khác"

    fun seed(): List<CategoryEntity> = listOf(
        CategoryEntity(displayName = "Ăn uống", icon = "🍜", colorHex = "#FF7043", isBuiltIn = true, sortOrder = 0),
        CategoryEntity(displayName = "Di chuyển", icon = "🚗", colorHex = "#42A5F5", isBuiltIn = true, sortOrder = 1),
        CategoryEntity(displayName = "Mua sắm", icon = "🛍️", colorHex = "#AB47BC", isBuiltIn = true, sortOrder = 2),
        CategoryEntity(displayName = "Hoá đơn & tiện ích", icon = "🧾", colorHex = "#26A69A", isBuiltIn = true, sortOrder = 3),
        CategoryEntity(displayName = "Giải trí", icon = "🎬", colorHex = "#EC407A", isBuiltIn = true, sortOrder = 4),
        CategoryEntity(displayName = "Sức khoẻ", icon = "💊", colorHex = "#66BB6A", isBuiltIn = true, sortOrder = 5),
        CategoryEntity(displayName = "Giáo dục", icon = "📚", colorHex = "#5C6BC0", isBuiltIn = true, sortOrder = 6),
        CategoryEntity(displayName = "Nhà cửa", icon = "🏠", colorHex = "#8D6E63", isBuiltIn = true, sortOrder = 7),
        CategoryEntity(displayName = "Du lịch", icon = "✈️", colorHex = "#29B6F6", isBuiltIn = true, sortOrder = 8),
        CategoryEntity(displayName = "Lương", icon = "💰", colorHex = "#FFCA28", isBuiltIn = true, sortOrder = 9),
        CategoryEntity(displayName = FALLBACK_NAME, icon = "📦", colorHex = "#9E9E9E", isBuiltIn = true, sortOrder = 10)
    )
}
