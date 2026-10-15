package com.example.bgps.domain

// Danh mục cố định cho bản dùng thử; nếu sau này cần người dùng tự tạo danh mục
// thì chuyển sang một bảng Room riêng thay vì enum này.
enum class Category(val displayName: String) {
    AN_UONG("Ăn uống"),
    DI_CHUYEN("Di chuyển"),
    MUA_SAM("Mua sắm"),
    HOA_DON("Hoá đơn & tiện ích"),
    GIAI_TRI("Giải trí"),
    SUC_KHOE("Sức khoẻ"),
    GIAO_DUC("Giáo dục"),
    NHA_CUA("Nhà cửa"),
    DU_LICH("Du lịch"),
    LUONG("Lương"),
    KHAC("Khác");

    companion object {
        val default: Category = KHAC

        fun fromDisplayName(name: String): Category =
            entries.find { it.displayName.equals(name.trim(), ignoreCase = true) } ?: default
    }
}
