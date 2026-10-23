package com.example.bgps.domain.ocr

// Kết quả rút ra từ chữ OCR đọc được trên hoá đơn. Cả hai trường đều có thể
// null nếu không tìm thấy — màn xác nhận luôn để người dùng tự nhập/sửa,
// không bao giờ coi đây là dữ liệu chắc chắn đúng.
data class ParsedReceipt(
    val amount: Long?,
    val date: Long?
)
