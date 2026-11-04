package com.example.bgps.ui.trips

import androidx.lifecycle.ViewModel
import com.example.bgps.data.database.FuelVehicleType
import com.example.bgps.domain.trip.TripPoint
import com.example.bgps.location.FuelSettings
import com.example.bgps.location.FuelSettingsStore
import com.example.bgps.location.LiveTripTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LiveTripViewModel @Inject constructor(
    tracker: LiveTripTracker,
    fuelSettingsStore: FuelSettingsStore
) : ViewModel() {
    val points: StateFlow<List<TripPoint>> = tracker.points
    val isTracking: StateFlow<Boolean> = tracker.isTracking
    val fuelVehicleType: StateFlow<FuelVehicleType?> = tracker.fuelVehicleType
    val fuelSettings: StateFlow<FuelSettings> = fuelSettingsStore.settings
}
