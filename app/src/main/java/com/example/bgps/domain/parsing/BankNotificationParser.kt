package com.example.bgps.domain.parsing

// Một app ngân hàng/ví ứng với một parser. Hàm thuần, không phụ thuộc Android
// nên viết test được bằng JVM (chạy ./gradlew test, không cần thiết bị).
interface BankNotificationParser {
    val packageName: String

    // Trả về null nếu không nhận diện được đây là thông báo giao dịch
    // (ví dụ thông báo khuyến mãi, OTP...).
    fun parse(title: String, text: String): ParsedTransaction?
}
