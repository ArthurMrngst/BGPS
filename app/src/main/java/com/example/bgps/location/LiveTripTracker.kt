package com.example.bgps.location

import com.example.bgps.data.database.FuelVehicleType
import com.example.bgps.domain.trip.TripPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Cầu nối giữa TripTrackingService (chạy nền) và màn hình bản đồ trực tiếp —
// Service ghi điểm vào đây mỗi khi có toạ độ mới, UI quan sát để vẽ/đuổi
// theo ngay lập tức. Không dùng DB cho việc này vì trip_points chỉ được lưu
// sau khi hành trình kết thúc (xem TripTrackingService.stopTracking).
@Singleton
class LiveTripTracker @Inject constructor() {
    private val _points = MutableStateFlow<List<TripPoint>>(emptyList())
    val points: StateFlow<List<TripPoint>> = _points.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _fuelVehicleType = MutableStateFlow<FuelVehicleType?>(null)
    val fuelVehicleType: StateFlow<FuelVehicleType?> = _fuelVehicleType.asStateFlow()

    fun start(fuelVehicleType: FuelVehicleType?) {
        _points.value = emptyList()
        _fuelVehicleType.value = fuelVehicleType
        _isTracking.value = true
    }

    fun addPoint(point: TripPoint) {
        _points.value = _points.value + point
    }

    fun stop() {
        _isTracking.value = false
        _points.value = emptyList()
        _fuelVehicleType.value = null
    }
}
