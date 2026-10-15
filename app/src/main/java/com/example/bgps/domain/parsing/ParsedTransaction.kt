package com.example.bgps.domain.parsing

import com.example.bgps.data.database.TransactionType

// Kết quả rút ra từ một thông báo ngân hàng/ví. timestamp = null nghĩa là không
// đọc được giờ giao dịch từ nội dung; nơi gọi (Service) tự dùng giờ đăng thông
// báo (sbn.postTime) làm phương án dự phòng.
data class ParsedTransaction(
    val amount: Long,
    val type: TransactionType,
    val merchant: String?,
    val timestamp: Long?
)
