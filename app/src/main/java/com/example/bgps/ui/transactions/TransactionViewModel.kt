package com.example.bgps.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionSource
import com.example.bgps.data.database.TransactionType
import com.example.bgps.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    // Room Flow -> StateFlow để Compose đọc được.
    // WhileSubscribed(5_000): ngừng lắng nghe DB sau 5 giây khi không còn UI quan sát.
    val transactions: StateFlow<List<TransactionEntity>> =
        repository.observeTransactions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun addTransaction(
        amount: Long,
        type: TransactionType,
        category: String,
        merchant: String?,
        note: String?
    ) {
        viewModelScope.launch {
            // id (UUID), updatedAt, isDeleted, isDirty đã có giá trị mặc định trong Entity
            repository.save(
                TransactionEntity(
                    amount = amount,
                    type = type,
                    category = category,
                    merchant = merchant,
                    note = note?.takeIf { it.isNotBlank() },
                    source = TransactionSource.MANUAL,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { repository.delete(transaction) }
    }
}
