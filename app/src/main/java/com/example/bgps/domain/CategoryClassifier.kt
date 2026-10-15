package com.example.bgps.domain

// Phân loại danh mục theo tên nơi giao dịch. Hàm thuần, không phụ thuộc Android
// nên viết test được; bảng từ khoá để riêng ở đây cho dễ thêm/bớt về sau.
object CategoryClassifier {

    private val rules: List<Pair<String, Category>> = listOf(
        "highlands" to Category.AN_UONG,
        "phuc long" to Category.AN_UONG,
        "starbucks" to Category.AN_UONG,
        "the coffee house" to Category.AN_UONG,
        "grab" to Category.DI_CHUYEN,
        "xanh sm" to Category.DI_CHUYEN,
        "mai linh" to Category.DI_CHUYEN,
        "shopee" to Category.MUA_SAM,
        "lazada" to Category.MUA_SAM,
        "tiki" to Category.MUA_SAM,
        "evn" to Category.HOA_DON,
        "viettel" to Category.HOA_DON,
        "fpt telecom" to Category.HOA_DON,
        "netflix" to Category.GIAI_TRI,
        "cgv" to Category.GIAI_TRI,
        "spotify" to Category.GIAI_TRI,
        "pharmacity" to Category.SUC_KHOE,
        "long chau" to Category.SUC_KHOE,
        "medicare" to Category.SUC_KHOE,
        "benh vien" to Category.SUC_KHOE,
        "phong kham" to Category.SUC_KHOE
    )

    // Trả về null nếu không khớp từ khoá nào, để nơi gọi tự quyết định giá trị mặc định.
    fun suggest(merchant: String?): Category? {
        if (merchant.isNullOrBlank()) return null
        val normalized = merchant.trim().lowercase()
        return rules.firstOrNull { (keyword, _) -> normalized.contains(keyword) }?.second
    }
}
