package com.example.bgps.domain

import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionSource

enum class DateRangePreset { ALL, LAST_7_DAYS, LAST_30_DAYS, THIS_MONTH }

data class TransactionFilter(
    val keyword: String = "",
    val category: String? = null,          // null = tất cả danh mục
    val source: TransactionSource? = null, // null = tất cả nguồn
    val dateRange: DateRangePreset = DateRangePreset.ALL
) {
    val isActive: Boolean
        get() = keyword.isNotBlank() || category != null || source != null || dateRange != DateRangePreset.ALL
}

// Hàm thuần: không phụ thuộc Android UI nên viết test được. Từ khoá tìm
// theo nơi giao dịch + ghi chú + danh mục, không phân biệt hoa/thường.
object TransactionFilterer {

    fun dateRangeStart(preset: DateRangePreset, now: Long = System.currentTimeMillis()): Long? =
        when (preset) {
            DateRangePreset.ALL -> null
            DateRangePreset.LAST_7_DAYS -> now - 7L * 24 * 60 * 60 * 1000
            DateRangePreset.LAST_30_DAYS -> now - 30L * 24 * 60 * 60 * 1000
            DateRangePreset.THIS_MONTH -> SummaryCalculator.periodStart(SummaryPeriod.MONTH)
        }

    fun apply(
        transactions: List<TransactionEntity>,
        filter: TransactionFilter,
        now: Long = System.currentTimeMillis()
    ): List<TransactionEntity> {
        val from = dateRangeStart(filter.dateRange, now)
        val keyword = filter.keyword.trim().lowercase()

        return transactions.filter { tx ->
            (filter.category == null || tx.category.equals(filter.category, ignoreCase = true)) &&
                (filter.source == null || tx.source == filter.source) &&
                (from == null || tx.timestamp >= from) &&
                (
                    keyword.isBlank() || listOfNotNull(tx.merchant, tx.note, tx.category)
                        .any { it.lowercase().contains(keyword) }
                    )
        }
    }
}
