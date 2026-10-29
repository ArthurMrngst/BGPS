package com.example.bgps.domain.parsing

import com.example.bgps.data.database.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TechcombankNotificationParserTest {

    // Mẫu THẬT bắt được qua `adb shell dumpsys notification` ngày 2026-10-30
    // — đúng mặt nhận tiền của giao dịch Liobank đã bắt trước đó.
    private val realIncomeTitle = "+ VND 30,000"
    private val realIncomeText = "Tài khoản: 19038124134011\nSố dư: VND 50,000\nNguyen Khanh Hung tu Liobank\n"

    @Test
    fun `parses the real captured income notification`() {
        val result = TechcombankNotificationParser.parse(realIncomeTitle, realIncomeText)

        requireNotNull(result)
        assertEquals(30_000L, result.amount)
        assertEquals(TransactionType.INCOME, result.type)
        assertEquals("Nguyen Khanh Hung tu Liobank", result.merchant)
        assertNull(result.timestamp)
    }

    @Test
    fun `parses a minus-sign title as an expense`() {
        // Chưa có mẫu chi tiêu thật từ Techcombank — suy đoán theo cùng quy
        // luật +/- đã thấy ở chiều nhận tiền.
        val result = TechcombankNotificationParser.parse(
            "- VND 45,000",
            "Tài khoản: 19038124134011\nSố dư: VND 5,000\nThanh toan POS\n"
        )

        requireNotNull(result)
        assertEquals(45_000L, result.amount)
        assertEquals(TransactionType.EXPENSE, result.type)
    }

    @Test
    fun `returns null when title has no amount`() {
        assertNull(TechcombankNotificationParser.parse("Techcombank", "Chào mừng bạn quay lại"))
    }
}
