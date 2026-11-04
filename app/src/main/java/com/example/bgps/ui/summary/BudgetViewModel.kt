package com.example.bgps.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgps.data.database.BudgetEntity
import com.example.bgps.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    val budgets: StateFlow<List<BudgetEntity>> =
        repository.observeBudgets()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    // monthlyLimit <= 0 nghĩa là bỏ ngân sách cho danh mục đó (xem
    // BudgetRepository.save).
    fun save(category: String, monthlyLimit: Long) {
        viewModelScope.launch { repository.save(category, monthlyLimit) }
    }
}
