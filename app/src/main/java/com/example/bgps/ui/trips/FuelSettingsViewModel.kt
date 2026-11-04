package com.example.bgps.ui.trips

import androidx.lifecycle.ViewModel
import com.example.bgps.location.FuelSettings
import com.example.bgps.location.FuelSettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class FuelSettingsViewModel @Inject constructor(
    private val store: FuelSettingsStore
) : ViewModel() {
    val settings: StateFlow<FuelSettings> = store.settings

    fun save(motorbikeLitersPer100Km: Double, carLitersPer100Km: Double, pricePerLiterVnd: Double) {
        store.save(
            FuelSettings(
                motorbikeLitersPer100Km = motorbikeLitersPer100Km,
                carLitersPer100Km = carLitersPer100Km,
                pricePerLiterVnd = pricePerLiterVnd
            )
        )
    }
}
