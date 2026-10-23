package com.example.bgps.domain.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ReceiptParserTest {

    // Mẫu tự soạn mô phỏng chữ OCR đọc từ hoá đơn giấy in nhiệt (thường mất
    // dấu tiếng Việt). CHƯA đối chiếu với hoá đơn thật — khi có, dán vào đây
    // rồi chỉnh lại regex trong ReceiptParser cho khớp.
    private val supermarketReceipt = """
        CO.OPMART
        Dia chi: 123 Nguyen Van Cu, Q5, TPHCM
        Hoa don ban hang
        Ngay: 15/10/2026  Gio: 14:32
        ----------------------------
        Sua tuoi Vinamilk 1L        32,000
        Banh mi sandwich            15,000
        Trung ga hop 10              28,500
        ----------------------------
        So luong: 3
        TONG CONG:                  75,500
        Tien khach dua:             100,000
        Tien thoi lai:               24,500
        Cam on quy khach
    """.trimIndent()

    private val restaurantReceipt = """
        QUAN AN GIA DINH
        123 Le Loi, Q1
        Ngay in: 16-10-2026 12:05
        Ban so: 5
        --------------------------
        Com tam suon                 45,000
        Nuoc ngot                    10,000
        --------------------------
        Thanh tien:                  55,000
        Xin cam on!
    """.trimIndent()

    private val pharmacyReceipt = """
        NHA THUOC LONG CHAU
        CN: 456 Vo Van Tan, Q3
        HOA DON GTGT
        Ngay: 01/10/2026
        --------------------------
        Paracetamol 500mg (hop)      35,000
        Vitamin C sui                 48,000
        --------------------------
        Tong thanh toan:              83,000
    """.trimIndent()

    @Test
    fun `parses supermarket receipt amount and date`() {
        val result = ReceiptParser.parse(supermarketReceipt)

        assertEquals(75_500L, result.amount)
        assertEquals(epochMillis(2026, 10, 15), result.date)
    }

    @Test
    fun `parses restaurant receipt with dash date and lowercase keyword`() {
        val result = ReceiptParser.parse(restaurantReceipt)

        assertEquals(55_000L, result.amount)
        assertEquals(epochMillis(2026, 10, 16), result.date)
    }

    @Test
    fun `parses pharmacy receipt using accented keyword`() {
        val result = ReceiptParser.parse(pharmacyReceipt)

        assertEquals(83_000L, result.amount)
        assertEquals(epochMillis(2026, 10, 1), result.date)
    }

    @Test
    fun `picks the last number on a line with a quantity calculation`() {
        val text = "TONG CONG: 3 x 25,000 = 75,000"

        val result = ReceiptParser.parse(text)

        assertEquals(75_000L, result.amount)
    }

    @Test
    fun `returns null amount when no keyword line is found`() {
        val result = ReceiptParser.parse("Sua tuoi Vinamilk 1L  32,000\nCam on quy khach")

        assertNull(result.amount)
    }

    @Test
    fun `returns null date when no date pattern is found`() {
        val result = ReceiptParser.parse("TONG CONG: 75,500")

        assertNull(result.date)
    }

    // Văn bản ML Kit Text Recognition thật trả về khi chạy trên ảnh hoá đơn tự
    // vẽ trên emulator (không phải đoán) — "TONG CONG:" và "47,000" bị tách
    // thành hai khối khác nhau, và "20" bị đọc thành "2O".
    @Test
    fun `handles real ML Kit output where amount is split from its label line`() {
        val mlKitOutput = """
            CO.OPMART
            Dia chi: 123 Nguyen Van Cu
            Hoa don ban hang
            Ngay: 2O/10/2026
            Sua tuoi Vinamilk 1L 32,000
            Banh mi sandwich
            TONG CONG:
            Cam on quy khach
            15,000
            47,000
        """.trimIndent()

        val result = ReceiptParser.parse(mlKitOutput)

        assertEquals(47_000L, result.amount)
        assertEquals(epochMillis(2026, 10, 20), result.date)
    }

    private fun epochMillis(year: Int, month: Int, day: Int): Long =
        LocalDate.of(year, month, day)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
}
