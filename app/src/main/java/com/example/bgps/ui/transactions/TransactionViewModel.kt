package com.example.bgps.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionSource
import com.example.bgps.data.database.TransactionType
import com.example.bgps.data.repository.CategoryRepository
import com.example.bgps.data.repository.TransactionRepository
import com.example.bgps.domain.TransactionFilter
import com.example.bgps.domain.TransactionFilterer
import com.example.bgps.location.CurrentLocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val currentLocationProvider: CurrentLocationProvider,
    categoryRepository: CategoryRepository
) : ViewModel() {

    val categoryNames: StateFlow<List<String>> =
        categoryRepository.observeCategories()
            .map { list -> list.map { it.displayName } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    // Room Flow -> StateFlow để Compose đọc được.
    // WhileSubscribed(5_000): ngừng lắng nghe DB sau 5 giây khi không còn UI quan sát.
    val transactions: StateFlow<List<TransactionEntity>> =
        repository.observeTransactions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val _filter = MutableStateFlow(TransactionFilter())
    val filter: StateFlow<TransactionFilter> = _filter.asStateFlow()

    // Danh sách sau khi lọc — màn danh sách hiện cái này thay vì `transactions`
    // thô; tự tính lại khi DB đổi HOẶC khi người dùng đổi bộ lọc.
    val filteredTransactions: StateFlow<List<TransactionEntity>> =
        combine(transactions, _filter) { list, filter ->
            TransactionFilterer.apply(list, filter)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setFilter(filter: TransactionFilter) {
        _filter.value = filter
    }

    fun addTransaction(
        amount: Long,
        type: TransactionType,
        category: String,
        merchant: String?,
        note: String?
    ) {
        viewModelScope.launch {
            val location = currentLocationProvider.getLastLocation()
            // id (UUID), updatedAt, isDeleted, isDirty đã có giá trị mặc định trong Entity
            repository.save(
                TransactionEntity(
                    amount = amount,
                    type = type,
                    category = category,
                    merchant = merchant,
                    note = note?.takeIf { it.isNotBlank() },
                    source = TransactionSource.MANUAL,
                    timestamp = System.currentTimeMillis(),
                    latitude = location?.first,
                    longitude = location?.second
                )
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { repository.delete(transaction) }
    }
}
