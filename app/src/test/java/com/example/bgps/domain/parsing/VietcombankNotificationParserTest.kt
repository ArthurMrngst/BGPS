package com.example.bgps.domain.parsing

import com.example.bgps.data.database.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class VietcombankNotificationParserTest {

    // Mẫu tự soạn, chưa đối chiếu thông báo thật (xem ghi chú trong parser).
    private val expenseText =
        "TK 0071000012345(VND) GD: -150,000VND luc 15-10-2026 14:32:05. " +
            "So du 2,345,678VND. Ref MBVCB.123456. Giao dich tai STARBUCKS HANOI"

    private val incomeText =
        "TK 0071000012345(VND) GD: +2,000,000VND luc 01-10-2026 08:00:00. " +
            "So du 4,345,678VND. Ref MBVCB.654321. Giao dich tai CONG TY ABC"

    @Test
    fun `parses an expense with amount, merchant and time`() {
        val result = VietcombankNotificationParser.parse("Thong bao giao dich", expenseText)

        requireNotNull(result)
        assertEquals(150_000L, result.amount)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("STARBUCKS HANOI", result.merchant)
        assertEquals(expectedEpochMillis(2026, 10, 15, 14, 32, 5), result.timestamp)
    }

    @Test
    fun `parses an income as a positive amount`() {
        val result = VietcombankNotificationParser.parse("Thong bao giao dich", incomeText)

        requireNotNull(result)
        assertEquals(2_000_000L, result.amount)
        assertEquals(TransactionType.INCOME, result.type)
        assertEquals("CONG TY ABC", result.merchant)
    }

    @Test
    fun `returns null for unrelated notifications`() {
        assertNull(VietcombankNotificationParser.parse("Khuyen mai", "Nhan ngay uu dai the tin dung moi!"))
    }

    @Test
    fun `still returns amount and type when time cannot be parsed`() {
        val text = "TK 0071000012345(VND) GD: -20,000VND. Giao dich tai XYZ"
        val result = VietcombankNotificationParser.parse("Thong bao", text)

        requireNotNull(result)
        assertEquals(20_000L, result.amount)
        assertTrue(result.timestamp == null)
    }

    private fun expectedEpochMillis(
        year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int
    ): Long =
        LocalDateTime.of(year, month, day, hour, minute, second)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
}
