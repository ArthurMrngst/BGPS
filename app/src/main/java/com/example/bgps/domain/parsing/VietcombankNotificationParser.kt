package com.example.bgps.domain.parsing

import com.example.bgps.data.database.TransactionType
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Mẫu tự soạn theo định dạng SMS/thông báo biến động số dư phổ biến của
// Vietcombank Digibank (không dấu, kiểu SMS cũ). CHƯA đối chiếu với thông báo
// thật vì Sprint 1 chưa gom được mẫu thật — khi có, dán vào
// VietcombankNotificationParserTest rồi chỉnh lại regex cho khớp.
// packageName cũng là phỏng đoán, cần xác nhận lại bằng
// `adb shell pm list packages | grep -i vcb` trên máy đã cài app thật.
object VietcombankNotificationParser : BankNotificationParser {
    override val packageName: String = "com.VCB"

    private val amountRegex = Regex("""GD:\s*([+-])\s*([\d.,]+)\s*VND""", RegexOption.IGNORE_CASE)
    private val timeRegex = Regex("""luc\s+(\d{2}-\d{2}-\d{4}\s+\d{2}:\d{2}:\d{2})""", RegexOption.IGNORE_CASE)
    private val merchantRegex = Regex("""tai\s+(.+)$""", RegexOption.IGNORE_CASE)
    private val timeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

    override fun parse(title: String, text: String): ParsedTransaction? {
        val amountMatch = amountRegex.find(text) ?: return null
        val amount = amountMatch.groupValues[2].filter { it.isDigit() }.toLongOrNull() ?: return null
        if (amount <= 0) return null
        val type = if (amountMatch.groupValues[1] == "-") TransactionType.EXPENSE else TransactionType.INCOME

        val timestamp = timeRegex.find(text)?.let { match ->
            runCatching {
                LocalDateTime.parse(match.groupValues[1], timeFormatter)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }.getOrNull()
        }

        val merchant = merchantRegex.find(text)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }

        return ParsedTransaction(amount = amount, type = type, merchant = merchant, timestamp = timestamp)
    }
}
