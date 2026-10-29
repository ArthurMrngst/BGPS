package com.example.bgps.domain.parsing

import com.example.bgps.data.database.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LiobankNotificationParserTest {

    // Mẫu THẬT bắt được qua `adb shell dumpsys notification` ngày 2026-10-30,
    // không phải tự soạn — xem ghi chú trong LiobankNotificationParser.
    private val realExpenseTitle = "‎💸 30 000₫"
    private val realExpenseText = "‎19038124134011\nSố dư 39.000₫"

    @Test
    fun `parses the real captured expense notification`() {
        val result = LiobankNotificationParser.parse(realExpenseTitle, realExpenseText)

        requireNotNull(result)
        assertEquals(30_000L, result.amount)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertNull(result.merchant)
        assertNull(result.timestamp)
    }

    @Test
    fun `treats a title without the expense icon as income`() {
        // Chưa có mẫu nhận tiền thật — đây là suy đoán tạm, xem ghi chú trong parser.
        val result = LiobankNotificationParser.parse("+50 000₫", "19038124134011\nSố dư 89.000₫")

        requireNotNull(result)
        assertEquals(50_000L, result.amount)
        assertEquals(TransactionType.INCOME, result.type)
    }

    @Test
    fun `returns null when title has no amount`() {
        assertNull(LiobankNotificationParser.parse("Liobank", "Chào mừng bạn quay lại"))
    }
}
