package com.example.bgps.domain

import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionSource
import com.example.bgps.data.database.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionFiltererTest {

    private fun tx(
        amount: Long = 10_000L,
        category: String = "Ăn uống",
        merchant: String? = null,
        note: String? = null,
        source: TransactionSource = TransactionSource.MANUAL,
        timestamp: Long = 10_000_000L
    ) = TransactionEntity(
        amount = amount,
        type = TransactionType.EXPENSE,
        category = category,
        merchant = merchant,
        note = note,
        source = source,
        timestamp = timestamp
    )

    @Test
    fun `keyword matches merchant case-insensitively`() {
        val list = listOf(
            tx(merchant = "Highlands Coffee"),
            tx(merchant = "Circle K")
        )

        val result = TransactionFilterer.apply(list, TransactionFilter(keyword = "highlands"))

        assertEquals(1, result.size)
        assertEquals("Highlands Coffee", result[0].merchant)
    }

    @Test
    fun `keyword also matches note and category`() {
        val list = listOf(
            tx(note = "trả tiền trọ"),
            tx(category = "Di chuyển")
        )

        assertEquals(1, TransactionFilterer.apply(list, TransactionFilter(keyword = "trọ")).size)
        assertEquals(1, TransactionFilterer.apply(list, TransactionFilter(keyword = "di chuyển")).size)
    }

    @Test
    fun `filters by exact category ignoring case`() {
        val list = listOf(tx(category = "Ăn uống"), tx(category = "Di chuyển"))

        val result = TransactionFilterer.apply(list, TransactionFilter(category = "ăn uống"))

        assertEquals(1, result.size)
        assertEquals("Ăn uống", result[0].category)
    }

    @Test
    fun `filters by source`() {
        val list = listOf(
            tx(source = TransactionSource.MANUAL),
            tx(source = TransactionSource.NOTIFICATION)
        )

        val result = TransactionFilterer.apply(list, TransactionFilter(source = TransactionSource.NOTIFICATION))

        assertEquals(1, result.size)
        assertEquals(TransactionSource.NOTIFICATION, result[0].source)
    }

    @Test
    fun `last 7 days excludes older transactions`() {
        val now = 100_000_000L
        val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
        val list = listOf(
            tx(timestamp = now - 1_000L),                 // trong 7 ngày
            tx(timestamp = now - sevenDaysMs - 10_000L)    // ngoài 7 ngày
        )

        val result = TransactionFilterer.apply(list, TransactionFilter(dateRange = DateRangePreset.LAST_7_DAYS), now = now)

        assertEquals(1, result.size)
    }

    @Test
    fun `combines all filters together`() {
        val list = listOf(
            tx(category = "Ăn uống", merchant = "Highlands", source = TransactionSource.MANUAL),
            tx(category = "Ăn uống", merchant = "Highlands", source = TransactionSource.NOTIFICATION),
            tx(category = "Di chuyển", merchant = "Highlands", source = TransactionSource.MANUAL)
        )

        val result = TransactionFilterer.apply(
            list,
            TransactionFilter(keyword = "highlands", category = "Ăn uống", source = TransactionSource.MANUAL)
        )

        assertEquals(1, result.size)
    }

    @Test
    fun `empty filter is not active and returns everything`() {
        val list = listOf(tx(), tx())

        assertFalse(TransactionFilter().isActive)
        assertEquals(2, TransactionFilterer.apply(list, TransactionFilter()).size)
    }

    @Test
    fun `any set field marks the filter active`() {
        assertTrue(TransactionFilter(keyword = "x").isActive)
        assertTrue(TransactionFilter(category = "Ăn uống").isActive)
        assertTrue(TransactionFilter(source = TransactionSource.OCR).isActive)
        assertTrue(TransactionFilter(dateRange = DateRangePreset.THIS_MONTH).isActive)
    }
}
