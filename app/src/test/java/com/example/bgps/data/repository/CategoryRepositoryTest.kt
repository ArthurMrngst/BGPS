package com.example.bgps.data.repository

import com.example.bgps.data.database.BudgetEntity
import com.example.bgps.data.database.CategoryEntity
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionType
import com.example.bgps.domain.DefaultCategories
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CategoryRepositoryTest {

    private lateinit var categoryDao: FakeCategoryDao
    private lateinit var transactionDao: FakeTransactionDao
    private lateinit var budgetDao: FakeBudgetDao
    private lateinit var repository: CategoryRepository

    @Before
    fun setUp() {
        categoryDao = FakeCategoryDao()
        transactionDao = FakeTransactionDao()
        budgetDao = FakeBudgetDao()
        repository = CategoryRepository(categoryDao, transactionDao, budgetDao)
    }

    @Test
    fun `ensureSeeded inserts the 11 default categories only when the table is empty`() = runTest {
        repository.ensureSeeded()
        assertEquals(11, repository.observeCategories().first().size)

        // Gọi lại lần 2 không được thêm trùng.
        repository.ensureSeeded()
        assertEquals(11, repository.observeCategories().first().size)
    }

    @Test
    fun `rename moves matching transactions and the budget to the new name`() = runTest {
        val category = CategoryEntity(displayName = "Thú cưng")
        categoryDao.upsert(category)
        transactionDao.upsert(
            TransactionEntity(amount = 50_000L, type = TransactionType.EXPENSE, category = "Thú cưng", timestamp = 1_000L)
        )
        budgetDao.upsert(BudgetEntity(category = "Thú cưng", monthlyLimit = 300_000L))

        repository.rename(category, "Chó mèo")

        val transactions = transactionDao.observeAll().first()
        assertEquals("Chó mèo", transactions[0].category)
        assertTrue(transactions[0].isDirty)

        assertNull(budgetDao.getByCategory("Thú cưng"))
        assertEquals(300_000L, budgetDao.getByCategory("Chó mèo")?.monthlyLimit)

        val renamed = categoryDao.observeAll().first().find { it.id == category.id }
        assertEquals("Chó mèo", renamed?.displayName)
    }

    @Test
    fun `delete reassigns transactions and budget to the chosen category then removes the row`() = runTest {
        val category = CategoryEntity(displayName = "Quán A")
        categoryDao.upsert(category)
        transactionDao.upsert(
            TransactionEntity(amount = 20_000L, type = TransactionType.EXPENSE, category = "Quán A", timestamp = 1_000L)
        )
        budgetDao.upsert(BudgetEntity(category = "Quán A", monthlyLimit = 100_000L))

        repository.delete(category, reassignTo = DefaultCategories.FALLBACK_NAME)

        val transactions = transactionDao.observeAll().first()
        assertEquals(DefaultCategories.FALLBACK_NAME, transactions[0].category)
        assertNull(budgetDao.getByCategory("Quán A"))
        assertTrue(categoryDao.observeAll().first().none { it.id == category.id })
    }

    @Test
    fun `cannot delete the fallback category`() = runTest {
        val fallback = CategoryEntity(displayName = DefaultCategories.FALLBACK_NAME)
        categoryDao.upsert(fallback)
        transactionDao.upsert(
            TransactionEntity(
                amount = 20_000L,
                type = TransactionType.EXPENSE,
                category = DefaultCategories.FALLBACK_NAME,
                timestamp = 1_000L
            )
        )

        repository.delete(fallback, reassignTo = "Ăn uống")

        assertTrue(categoryDao.observeAll().first().any { it.id == fallback.id })
        assertEquals(DefaultCategories.FALLBACK_NAME, transactionDao.observeAll().first()[0].category)
    }
}
