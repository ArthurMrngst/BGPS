package com.example.bgps.domain.parsing

import com.example.bgps.data.database.TransactionType

// Dựa trên mẫu THẬT bắt được từ app Liobank (OCB) ngày 2026-10-30, không
// phải đoán như Vietcombank/Momo:
//   title = "💸 30 000₫"
//   text  = "19038124134011\nSố dư 39.000₫"
// Số tiền nằm ở TIÊU ĐỀ (không phải nội dung), dùng dấu cách làm phân cách
// hàng nghìn ("30 000₫"), khác hẳn kiểu "30,000" hay "30.000" của các ngân
// hàng khác. Dòng đầu của text là số tham chiếu/tài khoản, không phải tên
// nơi giao dịch nên không rút merchant từ đây (dễ rút sai hơn là để trống).
//
// Chưa có mẫu thông báo NHẬN tiền thật — biểu tượng "💸" (tiền bay đi) được
// coi là dấu hiệu của khoản CHI; không có biểu tượng đó thì tạm coi là
// khoản THU. Đây là suy đoán tốt nhất có thể với 1 mẫu, cần xác nhận lại
// khi có thông báo nhận tiền thật.
object LiobankNotificationParser : BankNotificationParser {
    override val packageName: String = "com.ocb.liobank"

    private val amountRegex = Regex("""([\d][\d.,\s]*\d|\d)\s*[₫đ]""", RegexOption.IGNORE_CASE)
    private const val EXPENSE_ICON = "💸" // 💸

    override fun parse(title: String, text: String): ParsedTransaction? {
        val match = amountRegex.find(title) ?: return null
        val amount = match.groupValues[1].filter { it.isDigit() }.toLongOrNull() ?: return null
        if (amount <= 0) return null

        val type = if (title.contains(EXPENSE_ICON)) TransactionType.EXPENSE else TransactionType.INCOME

        return ParsedTransaction(amount = amount, type = type, merchant = null, timestamp = null)
    }
}
