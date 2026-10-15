package com.example.bgps.domain

import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

enum class SummaryPeriod { WEEK, MONTH }

data class CategoryTotal(
    val category: String,
    val total: Long,
    val percent: Float      // tỉ lệ trên tổng chi, từ 0.0 đến 1.0
)

data class SpendingSummary(
    val totalExpense: Long,
    val totalIncome: Long,
    val byCategory: List<CategoryTotal>
)

// Hàm thuần: không phụ thuộc Android UI nên viết test được
object SummaryCalculator {

    // Mốc bắt đầu của kỳ: đầu tuần (thứ Hai) hoặc ngày 1 của tháng, tính theo múi giờ máy
    fun periodStart(period: SummaryPeriod, zone: ZoneId = ZoneId.systemDefault()): Long {
        val today = LocalDate.now(zone)
        val startDate = when (period) {
            SummaryPeriod.WEEK -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            SummaryPeriod.MONTH -> today.withDayOfMonth(1)
        }
        return startDate.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun summarize(transactions: List<TransactionEntity>, from: Long): SpendingSummary {
        val inRange = transactions.filter { it.timestamp >= from }
        val expenses = inRange.filter { it.type == TransactionType.EXPENSE }

        val totalExpense = expenses.sumOf { it.amount }
        val totalIncome = inRange.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

        val byCategory = expenses
            // Danh mục đang là chữ tự gõ nên gộp không phân biệt hoa/thường và khoảng trắng thừa.
            // Sau này có bộ chọn danh mục (B3) thì không cần bước này nữa.
            .groupBy { it.category.trim().lowercase() }
            .map { (_, items) ->
                val total = items.sumOf { it.amount }
                CategoryTotal(
                    category = items.first().category.trim(),
                    total = total,
                    percent = if (totalExpense > 0) total.toFloat() / totalExpense else 0f
                )
            }
            .sortedByDescending { it.total }

        return SpendingSummary(totalExpense, totalIncome, byCategory)
    }
}
