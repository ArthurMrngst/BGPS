package com.example.bgps.data.repository

import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionSource
import com.example.bgps.data.database.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TransactionRepositoryTest {

    private lateinit var dao: FakeTransactionDao
    private lateinit var repository: TransactionRepository

    @Before
    fun setUp() {
        dao = FakeTransactionDao()
        repository = TransactionRepository(dao)
    }

    @Test
    fun `update stamps updatedAt and isDirty while keeping the same id`() = runTest {
        val original = TransactionEntity(
            amount = 50_000L,
            type = TransactionType.EXPENSE,
            category = "Ăn uống",
            timestamp = 1_000L,
            updatedAt = 1_000L,
            isDirty = false
        )
        repository.save(original)

        repository.update(original.copy(amount = 80_000L, category = "Di chuyển"))

        val stored = repository.getById(original.id)
        assertEquals(original.id, stored?.id)
        assertEquals(80_000L, stored?.amount)
        assertEquals("Di chuyển", stored?.category)
        assertTrue(stored!!.isDirty)
        assertTrue("updatedAt phải được làm mới, không giữ giá trị cũ 1000", stored.updatedAt > 1_000L)
    }

    @Test
    fun `soft-deleted transaction does not show up in the observed list`() = runTest {
        val tx = TransactionEntity(
            amount = 20_000L,
            type = TransactionType.EXPENSE,
            category = "Ăn uống",
            timestamp = 1_000L
        )
        repository.save(tx)
        assertEquals(1, repository.observeTransactions().first().size)

        repository.delete(tx)

        assertTrue(repository.observeTransactions().first().isEmpty())
    }

    @Test
    fun `finds a duplicate by matching transactionRef`() = runTest {
        repository.save(
            TransactionEntity(
                amount = 30_000L,
                type = TransactionType.EXPENSE,
                category = "Ăn uống",
                source = TransactionSource.NOTIFICATION,
                timestamp = 1_000_000L,
                transactionRef = "FT26303ABCDE"
            )
        )

        val duplicate = repository.findDuplicateNotificationTransaction(
            transactionRef = "FT26303ABCDE",
            amount = 30_000L,
            type = TransactionType.EXPENSE,
            timestamp = 9_999_999L // mốc thời gian khác hẳn, chỉ mã trùng vẫn phải bắt được
        )

        assertEquals("FT26303ABCDE", duplicate?.transactionRef)
    }

    @Test
    fun `finds a duplicate by amount, type and a close timestamp when there is no ref`() = runTest {
        repository.save(
            TransactionEntity(
                amount = 30_000L,
                type = TransactionType.EXPENSE,
                category = "Ăn uống",
                source = TransactionSource.NOTIFICATION,
                timestamp = 1_000_000L
            )
        )

        val duplicate = repository.findDuplicateNotificationTransaction(
            transactionRef = null,
            amount = 30_000L,
            type = TransactionType.EXPENSE,
            timestamp = 1_000_000L + 30_000L // 30 giây sau, trong cửa sổ 2 phút
        )

        assertEquals(30_000L, duplicate?.amount)
    }

    @Test
    fun `does not treat a different transaction far away in time as a duplicate`() = runTest {
        repository.save(
            TransactionEntity(
                amount = 30_000L,
                type = TransactionType.EXPENSE,
                category = "Ăn uống",
                source = TransactionSource.NOTIFICATION,
                timestamp = 1_000_000L
            )
        )

        val duplicate = repository.findDuplicateNotificationTransaction(
            transactionRef = null,
            amount = 30_000L,
            type = TransactionType.EXPENSE,
            timestamp = 1_000_000L + 10 * 60 * 1000L // 10 phút sau, ngoài cửa sổ
        )

        assertNull(duplicate)
    }

    @Test
    fun `does not treat a manual transaction as a duplicate of a notification one`() = runTest {
        repository.save(
            TransactionEntity(
                amount = 30_000L,
                type = TransactionType.EXPENSE,
                category = "Ăn uống",
                source = TransactionSource.MANUAL,
                timestamp = 1_000_000L
            )
        )

        val duplicate = repository.findDuplicateNotificationTransaction(
            transactionRef = null,
            amount = 30_000L,
            type = TransactionType.EXPENSE,
            timestamp = 1_000_000L
        )

        assertNull(duplicate)
    }
}
