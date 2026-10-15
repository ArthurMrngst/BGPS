package com.example.bgps.domain.parsing

import com.example.bgps.data.database.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class MomoNotificationParserTest {

    // Mẫu tự soạn, chưa đối chiếu thông báo thật (xem ghi chú trong parser).
    private val paymentText = "Bạn đã thanh toán 45,000đ cho GRABFOOD lúc 14:32 15/10/2026"
    private val receiveText = "Bạn đã nhận 200,000đ từ NGUYEN VAN A lúc 09:15 14/10/2026"

    @Test
    fun `parses a payment as an expense`() {
        val result = MomoNotificationParser.parse("Giao dịch thành công", paymentText)

        requireNotNull(result)
        assertEquals(45_000L, result.amount)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("GRABFOOD", result.merchant)
        assertEquals(expectedEpochMillis(2026, 10, 15, 14, 32), result.timestamp)
    }

    @Test
    fun `parses a received amount as income`() {
        val result = MomoNotificationParser.parse("Nhận tiền thành công", receiveText)

        requireNotNull(result)
        assertEquals(200_000L, result.amount)
        assertEquals(TransactionType.INCOME, result.type)
        assertEquals("NGUYEN VAN A", result.merchant)
    }

    @Test
    fun `returns null for unrelated notifications`() {
        assertNull(MomoNotificationParser.parse("Ưu đãi", "Nạp thẻ ngay để nhận voucher 50k!"))
    }

    private fun expectedEpochMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        LocalDateTime.of(year, month, day, hour, minute)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
}
