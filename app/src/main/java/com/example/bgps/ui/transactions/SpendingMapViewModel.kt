package com.example.bgps.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SpendingMapViewModel @Inject constructor(
    repository: TransactionRepository
) : ViewModel() {
    // Chỉ những giao dịch đã có toạ độ (nhập tay/OCR/thông báo đều có thể có
    // từ CurrentLocationProvider) mới vẽ được lên bản đồ.
    val locatedTransactions: StateFlow<List<TransactionEntity>> =
        repository.observeTransactions()
            .map { list -> list.filter { it.latitude != null && it.longitude != null } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
}
