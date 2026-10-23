package com.example.bgps.domain.ocr

import java.time.LocalDate
import java.time.ZoneId

// Rút số tiền và ngày từ chữ OCR đọc được trên hoá đơn. Hàm thuần, không phụ
// thuộc Android/ML Kit nên viết test được bằng JVM.
//
// Mẫu dùng để viết regex này là TỰ SOẠN (chưa có hoá đơn thật để đối chiếu —
// xem ghi chú ở ReceiptParserTest). Hoá đơn thật in bằng máy in nhiệt thường
// không có dấu tiếng Việt nên bảng từ khoá bên dưới có cả hai dạng có dấu/
// không dấu. Đây chỉ là gợi ý ban đầu — màn xác nhận luôn cho người dùng sửa
// tay vì OCR hoá đơn thật (nét mờ, cong vênh) dễ sai hơn nhiều so với mẫu test.
//
// Hai điều chỉnh dưới đây đến từ lần thử thật đầu tiên trên emulator (ảnh
// hoá đơn tự vẽ, chạy qua ML Kit thật — không phải chỉ đoán):
// 1. ML Kit hay tách nhãn ("TONG CONG:") và số tiền thành các dòng/khối khác
//    nhau khi giữa chúng có khoảng trắng lớn (số tiền canh phải), nên không
//    thể chỉ tìm số trên CÙNG dòng với từ khoá.
// 2. ML Kit từng đọc "20" thành "2O" (chữ O) trong ngày — lỗi OCR phổ biến
//    giữa số 0 và chữ O.
object ReceiptParser {

    private val amountKeywords = listOf(
        "tong cong", "tổng cộng",
        "tong tien", "tổng tiền",
        "tong thanh toan", "tổng thanh toán",
        "thanh tien", "thành tiền",
        "total"
    )
    private val numberTokenRegex = Regex("""[\d.,]{3,}""")
    private val dateRegex = Regex("""([\dOo]{1,2})[/.\-]([\dOo]{1,2})[/.\-]([\dOo]{2,4})""")

    fun parse(ocrText: String): ParsedReceipt =
        ParsedReceipt(amount = extractAmount(ocrText), date = extractDate(ocrText))

    private fun extractAmount(text: String): Long? {
        val candidateLine = text.lineSequence().firstOrNull { line ->
            val normalized = line.trim().lowercase()
            amountKeywords.any { keyword -> normalized.contains(keyword) }
        } ?: return null

        // Dòng tổng tiền có thể có nhiều số (VD "3 x 25,000 = 75,000") — lấy số
        // cuối cùng vì bố cục hoá đơn thường đặt giá trị thật ở bên phải.
        val sameLineNumber = numberTokenRegex.findAll(candidateLine).lastOrNull()?.value

        // Không có số nào trên cùng dòng với từ khoá (OCR tách thành khối
        // khác) — dùng số cuối cùng xuất hiện trong toàn bộ văn bản, vì tổng
        // tiền thường được in sau cùng trên hoá đơn.
        val numberText = sameLineNumber ?: numberTokenRegex.findAll(text).lastOrNull()?.value

        return numberText?.filter { it.isDigit() }?.toLongOrNull()?.takeIf { it > 0 }
    }

    private fun extractDate(text: String): Long? {
        val match = dateRegex.find(text) ?: return null
        val day = normalizeDigits(match.groupValues[1]).toIntOrNull() ?: return null
        val month = normalizeDigits(match.groupValues[2]).toIntOrNull() ?: return null
        var year = normalizeDigits(match.groupValues[3]).toIntOrNull() ?: return null
        if (year < 100) year += 2000

        return runCatching {
            LocalDate.of(year, month, day)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }

    private fun normalizeDigits(token: String): String = token.replace('O', '0').replace('o', '0')
}
