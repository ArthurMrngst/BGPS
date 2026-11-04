package com.example.bgps.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgps.data.repository.BudgetRepository
import com.example.bgps.data.repository.TransactionRepository
import com.example.bgps.domain.SpendingSummary
import com.example.bgps.domain.SummaryCalculator
import com.example.bgps.domain.SummaryPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    repository: TransactionRepository,
    budgetRepository: BudgetRepository
) : ViewModel() {

    private val _period = MutableStateFlow(SummaryPeriod.MONTH)
    val period: StateFlow<SummaryPeriod> = _period.asStateFlow()

    // combine: tự tính lại khi DB đổi, người dùng đổi tuần/tháng, hoặc đổi
    // ngân sách (D1).
    val summary: StateFlow<SpendingSummary> =
        combine(
            repository.observeTransactions(),
            _period,
            budgetRepository.observeBudgets()
        ) { transactions, period, budgets ->
            SummaryCalculator.summarize(
                transactions = transactions,
                from = SummaryCalculator.periodStart(period),
                period = period,
                budgets = budgets.associate { it.category to it.monthlyLimit }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SpendingSummary(0L, 0L, emptyList())
        )

    fun setPeriod(period: SummaryPeriod) {
        _period.value = period
    }
}