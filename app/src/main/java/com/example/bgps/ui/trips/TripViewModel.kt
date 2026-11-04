package com.example.bgps.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgps.data.database.TripEntity
import com.example.bgps.data.repository.TripRepository
import com.example.bgps.location.FuelSettings
import com.example.bgps.location.FuelSettingsStore
import com.example.bgps.location.LiveTripTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    repository: TripRepository,
    tracker: LiveTripTracker,
    fuelSettingsStore: FuelSettingsStore
) : ViewModel() {
    val trips: StateFlow<List<TripEntity>> =
        repository.observeTrips()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val isTracking: StateFlow<Boolean> = tracker.isTracking
    val fuelSettings: StateFlow<FuelSettings> = fuelSettingsStore.settings
}
