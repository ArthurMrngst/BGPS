package com.example.bgps.domain.parsing

import com.example.bgps.data.database.TransactionType

// Dựa trên mẫu THẬT bắt được từ app Techcombank ngày 2026-10-30 (giao dịch
// nhận tiền, đúng mặt bên kia của giao dịch Liobank đã bắt trước đó):
//   title = "+ VND 30,000"
//   text  = "Tài khoản: 19038124134011\nSố dư: VND 50,000\nNguyen Khanh Hung tu Liobank\n"
// Số tiền + dấu +/- nằm ở TIÊU ĐỀ, dùng dấu phẩy phân cách hàng nghìn
// ("30,000") — khác cả Vietcombank (dấu phẩy nhưng ở nội dung) lẫn Liobank
// (dấu cách, ở tiêu đề nhưng không có dấu +/-).
object TechcombankNotificationParser : BankNotificationParser {
    override val packageName: String = "vn.com.techcombank.bb.app"

    private val amountRegex = Regex("""([+-])\s*VND\s*([\d.,\s]+)""", RegexOption.IGNORE_CASE)

    override fun parse(title: String, text: String): ParsedTransaction? {
        val match = amountRegex.find(title) ?: return null
        val amount = match.groupValues[2].filter { it.isDigit() }.toLongOrNull() ?: return null
        if (amount <= 0) return null

        val type = if (match.groupValues[1] == "-") TransactionType.EXPENSE else TransactionType.INCOME

        // Dòng thứ 3 của nội dung (nếu có) thường là tên người chuyển/nhận,
        // VD "Nguyen Khanh Hung tu Liobank" — không phải lúc nào cũng là tên
        // cửa hàng nên B2 có thể không gợi ý đúng danh mục, nhưng vẫn hơn để
        // trống vì người dùng thấy được ngữ cảnh giao dịch khi xác nhận.
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val merchant = lines.getOrNull(2)

        return ParsedTransaction(amount = amount, type = type, merchant = merchant, timestamp = null)
    }
}
