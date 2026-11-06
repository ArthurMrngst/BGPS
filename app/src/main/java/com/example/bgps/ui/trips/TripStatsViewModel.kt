package com.example.bgps.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgps.data.repository.TripRepository
import com.example.bgps.domain.SummaryCalculator
import com.example.bgps.domain.SummaryPeriod
import com.example.bgps.domain.trip.TripStats
import com.example.bgps.domain.trip.TripStatsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TripStatsViewModel @Inject constructor(
    repository: TripRepository
) : ViewModel() {

    private val _period = MutableStateFlow(SummaryPeriod.MONTH)
    val period: StateFlow<SummaryPeriod> = _period.asStateFlow()

    val stats: StateFlow<TripStats> =
        combine(repository.observeTrips(), _period) { trips, period ->
            TripStatsCalculator.summarize(trips, SummaryCalculator.periodStart(period))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TripStats(0, 0.0, emptyList())
        )

    fun setPeriod(period: SummaryPeriod) {
        _period.value = period
    }
}
