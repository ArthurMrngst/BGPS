package com.example.bgps.data.repository

import com.example.bgps.data.database.BudgetDao
import com.example.bgps.data.database.CategoryDao
import com.example.bgps.data.database.CategoryEntity
import com.example.bgps.data.database.TransactionDao
import com.example.bgps.domain.DefaultCategories
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    // Gọi từ BGpsApplication.onCreate — chèn 11 danh mục mặc định nếu bảng
    // đang trống (cài mới HOẶC vừa nâng cấp DB qua MIGRATION_6_7, bảng đó
    // tạo ra trống không có sẵn dữ liệu).
    suspend fun ensureSeeded() {
        if (categoryDao.count() == 0) {
            categoryDao.upsertAll(DefaultCategories.seed())
        }
    }

    suspend fun add(displayName: String, icon: String?, colorHex: String?) {
        val name = displayName.trim()
        if (name.isBlank()) return
        categoryDao.upsert(CategoryEntity(displayName = name, icon = icon, colorHex = colorHex))
    }

    // Đổi tên: cập nhật dòng danh mục, đồng thời chuyển mọi giao dịch đang
    // gắn tên cũ sang tên mới (TransactionEntity.category chỉ là chuỗi, không
    // phải khoá ngoại) và dời luôn ngân sách (khoá chính của budgets chính
    // là tên danh mục) để không mất hạn mức đã đặt.
    suspend fun rename(category: CategoryEntity, newDisplayName: String) {
        val newName = newDisplayName.trim()
        if (newName.isBlank() || newName == category.displayName) return

        val now = System.currentTimeMillis()
        transactionDao.renameCategory(category.displayName, newName, now)

        budgetDao.getByCategory(category.displayName)?.let { budget ->
            budgetDao.delete(category.displayName)
            budgetDao.upsert(budget.copy(category = newName))
        }

        categoryDao.upsert(category.copy(displayName = newName))
    }

    suspend fun updateIconColor(category: CategoryEntity, icon: String?, colorHex: String?) {
        categoryDao.upsert(category.copy(icon = icon, colorHex = colorHex))
    }

    // Xoá danh mục: không xoá cứng giao dịch/ngân sách đang dùng nó, mà gộp
    // sang reassignTo trước (thường là "Khác"). Không cho xoá "Khác" — luôn
    // cần một đích gộp mặc định tồn tại.
    suspend fun delete(category: CategoryEntity, reassignTo: String) {
        if (category.displayName == DefaultCategories.FALLBACK_NAME) return

        val now = System.currentTimeMillis()
        transactionDao.renameCategory(category.displayName, reassignTo, now)
        budgetDao.delete(category.displayName)
        categoryDao.delete(category.id)
    }
}
