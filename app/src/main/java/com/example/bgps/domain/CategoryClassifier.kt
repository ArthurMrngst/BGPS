package com.example.bgps.domain

// Phân loại danh mục theo tên nơi giao dịch. Hàm thuần, không phụ thuộc Android
// nên viết test được; bảng từ khoá để riêng ở đây cho dễ thêm/bớt về sau.
//
// Trả tên danh mục dạng chuỗi (khớp CategoryEntity.displayName) thay vì enum
// — từ B3 phần 2, danh mục là dữ liệu người dùng tự quản lý, không còn cố
// định. Các từ khoá dưới đây gắn với 11 danh mục mặc định (DefaultCategories);
// nếu người dùng đổi tên/xoá các danh mục đó, gợi ý này có thể trỏ tới một
// tên không còn tồn tại — nơi gọi tự xử lý như một chuỗi category bình
// thường, không bắt buộc phải khớp danh mục có sẵn.
object CategoryClassifier {

    private val rules: List<Pair<String, String>> = listOf(
        "highlands" to "Ăn uống",
        "phuc long" to "Ăn uống",
        "starbucks" to "Ăn uống",
        "the coffee house" to "Ăn uống",
        "grab" to "Di chuyển",
        "xanh sm" to "Di chuyển",
        "mai linh" to "Di chuyển",
        "shopee" to "Mua sắm",
        "lazada" to "Mua sắm",
        "tiki" to "Mua sắm",
        "evn" to "Hoá đơn & tiện ích",
        "viettel" to "Hoá đơn & tiện ích",
        "fpt telecom" to "Hoá đơn & tiện ích",
        "netflix" to "Giải trí",
        "cgv" to "Giải trí",
        "spotify" to "Giải trí",
        "pharmacity" to "Sức khoẻ",
        "long chau" to "Sức khoẻ",
        "medicare" to "Sức khoẻ",
        "benh vien" to "Sức khoẻ",
        "phong kham" to "Sức khoẻ"
    )

    // Trả về null nếu không khớp từ khoá nào, để nơi gọi tự quyết định giá trị mặc định.
    fun suggest(merchant: String?): String? {
        if (merchant.isNullOrBlank()) return null
        val normalized = merchant.trim().lowercase()
        return rules.firstOrNull { (keyword, _) -> normalized.contains(keyword) }?.second
    }
}
