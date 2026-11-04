package com.example.bgps.data.repository

import com.example.bgps.data.database.BudgetDao
import com.example.bgps.data.database.BudgetEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val dao: BudgetDao
) {
    fun observeBudgets(): Flow<List<BudgetEntity>> = dao.observeAll()

    suspend fun save(category: String, monthlyLimit: Long) {
        if (monthlyLimit <= 0) {
            dao.delete(category)
        } else {
            dao.upsert(BudgetEntity(category, monthlyLimit))
        }
    }
}
