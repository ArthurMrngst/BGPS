package com.example.bgps.domain

import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionType
import org.junit.Assert
import org.junit.Test
import kotlin.collections.get
import kotlin.text.category

class SummaryCalculatorTest {

    private fun tx(amount: Long, type: TransactionType, category: String, timestamp: Long) =
        TransactionEntity(
            amount = amount,
            type = type,
            category = category,
            timestamp = timestamp
        )

    @Test
    fun `groups expenses by category and sorts descending`() {
        val list = listOf(
            tx(50_000L, TransactionType.EXPENSE, "Ăn uống", 1_000L),
            tx(30_000L, TransactionType.EXPENSE, "ăn uống ", 2_000L),   // cùng danh mục, khác hoa/thường
            tx(20_000L, TransactionType.EXPENSE, "Di chuyển", 3_000L),
            tx(1_000_000L, TransactionType.INCOME, "Lương", 4_000L)
        )

        val result = SummaryCalculator.summarize(list, from = 0L)

        Assert.assertEquals(100_000L, result.totalExpense)
        Assert.assertEquals(1_000_000L, result.totalIncome)
        Assert.assertEquals(2, result.byCategory.size)
        Assert.assertEquals(80_000L, result.byCategory[0].total)
        Assert.assertEquals(0.8f, result.byCategory[0].percent, 0.001f)
        Assert.assertEquals("Di chuyển", result.byCategory[1].category)
    }

    @Test
    fun `ignores transactions before the period start`() {
        val list = listOf(
            tx(50_000L, TransactionType.EXPENSE, "Ăn uống", 1_000L),
            tx(20_000L, TransactionType.EXPENSE, "Di chuyển", 3_000L)
        )

        val result = SummaryCalculator.summarize(list, from = 2_500L)

        Assert.assertEquals(20_000L, result.totalExpense)
        Assert.assertEquals(1, result.byCategory.size)
    }

    @Test
    fun `empty list gives zero totals`() {
        val result = SummaryCalculator.summarize(emptyList(), from = 0L)

        Assert.assertEquals(0L, result.totalExpense)
        Assert.assertEquals(0L, result.totalIncome)
        Assert.assertEquals(0, result.byCategory.size)
    }
}