package com.example.bgps.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CategoryClassifierTest {

    @Test
    fun `matches known merchant keyword`() {
        assertEquals(Category.AN_UONG, CategoryClassifier.suggest("Highlands Coffee - Nguyen Hue"))
        assertEquals(Category.DI_CHUYEN, CategoryClassifier.suggest("GRAB *TRIP HCM"))
        assertEquals(Category.SUC_KHOE, CategoryClassifier.suggest("Nha thuoc Long Chau 123"))
    }

    @Test
    fun `is case insensitive`() {
        assertEquals(Category.MUA_SAM, CategoryClassifier.suggest("shopee vietnam"))
        assertEquals(Category.MUA_SAM, CategoryClassifier.suggest("SHOPEE VIETNAM"))
    }

    @Test
    fun `returns null for unknown merchant`() {
        assertNull(CategoryClassifier.suggest("Cua hang tap hoa Minh Tam"))
    }

    @Test
    fun `returns null for blank or null merchant`() {
        assertNull(CategoryClassifier.suggest(null))
        assertNull(CategoryClassifier.suggest("   "))
    }

    @Test
    fun `hospital keyword is not shadowed by a shorter ride-hailing keyword`() {
        // "benh vien" chứa chuỗi con "be" (ứng dụng gọi xe); nếu có ai thêm "be" làm từ
        // khoá Di chuyển sau này, test này sẽ bắt được lỗi phân loại nhầm.
        assertEquals(Category.SUC_KHOE, CategoryClassifier.suggest("Benh vien Cho Ray"))
    }
}
