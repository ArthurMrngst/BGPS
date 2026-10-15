package com.example.bgps.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionType
import com.example.bgps.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: String = checkNotNull(savedStateHandle["transactionId"])

    // null = đang tải. Nếu id không tồn tại (ví dụ vừa bị xoá ở máy khác) thì vẫn là null
    // sau khi tải xong; màn hình tự đóng trong trường hợp đó.
    private val _transaction = MutableStateFlow<TransactionEntity?>(null)
    val transaction: StateFlow<TransactionEntity?> = _transaction.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private val _done = MutableStateFlow(false)
    val done: StateFlow<Boolean> = _done.asStateFlow()

    init {
        viewModelScope.launch {
            _transaction.value = repository.getById(transactionId)
            _loaded.value = true
        }
    }

    fun save(amount: Long, type: TransactionType, category: String, merchant: String?, note: String?) {
        val current = _transaction.value ?: return
        viewModelScope.launch {
            repository.update(
                current.copy(
                    amount = amount,
                    type = type,
                    category = category,
                    merchant = merchant,
                    note = note?.takeIf { it.isNotBlank() }
                )
            )
            _done.value = true
        }
    }

    fun delete() {
        val current = _transaction.value ?: return
        viewModelScope.launch {
            repository.delete(current)
            _done.value = true
        }
    }
}
