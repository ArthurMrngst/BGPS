package com.example.bgps.data.repository

import com.example.bgps.data.database.BudgetDao
import com.example.bgps.data.database.BudgetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Fake trong bộ nhớ thay cho Room, dùng cho unit test (JVM thuần).
class FakeBudgetDao : BudgetDao {
    private val state = MutableStateFlow<List<BudgetEntity>>(emptyList())

    override fun observeAll(): Flow<List<BudgetEntity>> = state

    override suspend fun getByCategory(category: String): BudgetEntity? =
        state.value.find { it.category == category }

    override suspend fun upsert(budget: BudgetEntity) {
        state.value = state.value.filterNot { it.category == budget.category } + budget
    }

    override suspend fun delete(category: String) {
        state.value = state.value.filterNot { it.category == category }
    }
}
