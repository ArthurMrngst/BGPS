package com.example.bgps.domain.parsing

import com.example.bgps.data.database.TransactionType
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Mẫu tự soạn theo định dạng thông báo đẩy (push, có dấu) phổ biến của app
// Momo. CHƯA đối chiếu với thông báo thật vì Sprint 1 chưa gom được mẫu thật —
// khi có, dán vào MomoNotificationParserTest rồi chỉnh lại regex cho khớp.
// packageName cũng là phỏng đoán, cần xác nhận lại bằng
// `adb shell pm list packages | grep -i momo` trên máy đã cài app thật.
object MomoNotificationParser : BankNotificationParser {
    override val packageName: String = "com.mservice.momotransfer"

    private val paymentRegex =
        Regex("""thanh toán\s+([\d.,]+)\s*đ\s+cho\s+(.+?)\s+lúc""", RegexOption.IGNORE_CASE)
    private val receiveRegex =
        Regex("""nhận\s+([\d.,]+)\s*đ\s+từ\s+(.+?)\s+lúc""", RegexOption.IGNORE_CASE)
    private val timeRegex = Regex("""lúc\s+(\d{2}:\d{2})\s+(\d{2}/\d{2}/\d{4})""")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")

    override fun parse(title: String, text: String): ParsedTransaction? {
        val (match, type) = paymentRegex.find(text)?.let { it to TransactionType.EXPENSE }
            ?: receiveRegex.find(text)?.let { it to TransactionType.INCOME }
            ?: return null

        val amount = match.groupValues[1].filter { it.isDigit() }.toLongOrNull() ?: return null
        if (amount <= 0) return null
        val merchant = match.groupValues[2].trim().takeIf { it.isNotBlank() }

        val timestamp = timeRegex.find(text)?.let { m ->
            runCatching {
                LocalDateTime.parse("${m.groupValues[1]} ${m.groupValues[2]}", timeFormatter)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }.getOrNull()
        }

        return ParsedTransaction(amount = amount, type = type, merchant = merchant, timestamp = timestamp)
    }
}
