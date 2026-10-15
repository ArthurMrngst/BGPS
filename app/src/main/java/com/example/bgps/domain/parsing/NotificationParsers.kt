package com.example.bgps.domain.parsing

// Danh sách parser theo từng app ngân hàng/ví. Thêm app mới thì viết thêm một
// BankNotificationParser rồi đăng ký vào danh sách này — Service tự động
// whitelist theo packageName của các parser có ở đây, không cần sửa chỗ khác.
object NotificationParsers {
    private val all: List<BankNotificationParser> = listOf(
        VietcombankNotificationParser,
        MomoNotificationParser
    )

    val whitelistedPackages: Set<String> = all.map { it.packageName }.toSet()

    fun forPackage(packageName: String): BankNotificationParser? =
        all.find { it.packageName == packageName }
}
