package com.example.bgps.data.repository

import com.example.bgps.data.database.CategoryDao
import com.example.bgps.data.database.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Fake trong bộ nhớ thay cho Room, dùng cho unit test (JVM thuần).
class FakeCategoryDao : CategoryDao {
    private val state = MutableStateFlow<List<CategoryEntity>>(emptyList())

    override fun observeAll(): Flow<List<CategoryEntity>> = state

    override suspend fun count(): Int = state.value.size

    override suspend fun upsert(category: CategoryEntity) {
        state.value = state.value.filterNot { it.id == category.id } + category
    }

    override suspend fun upsertAll(categories: List<CategoryEntity>) {
        val existingIds = categories.map { it.id }.toSet()
        state.value = state.value.filterNot { it.id in existingIds } + categories
    }

    override suspend fun delete(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
}
