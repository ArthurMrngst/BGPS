package com.example.bgps.domain.trip

import com.example.bgps.data.database.TripEntity
import com.example.bgps.data.database.VehicleType

data class VehicleStats(
    val vehicleType: VehicleType,
    val tripCount: Int,
    val totalDistanceMeters: Double
)

data class TripStats(
    val tripCount: Int,
    val totalDistanceMeters: Double,
    val byVehicle: List<VehicleStats>
)

// Hàm thuần: không phụ thuộc Android UI nên viết test được. Dùng chung mốc
// đầu kỳ với SummaryCalculator.periodStart (tuần/tháng) vì ý nghĩa giống hệt
// — không cần một hàm tính mốc thời gian riêng cho hành trình.
object TripStatsCalculator {
    fun summarize(trips: List<TripEntity>, from: Long): TripStats {
        val inRange = trips.filter { it.startTime >= from }
        val totalDistance = inRange.sumOf { it.distanceMeters }

        val byVehicle = inRange
            .groupBy { it.vehicleType }
            .map { (type, items) ->
                VehicleStats(
                    vehicleType = type,
                    tripCount = items.size,
                    totalDistanceMeters = items.sumOf { it.distanceMeters }
                )
            }
            .sortedByDescending { it.totalDistanceMeters }

        return TripStats(inRange.size, totalDistance, byVehicle)
    }
}
