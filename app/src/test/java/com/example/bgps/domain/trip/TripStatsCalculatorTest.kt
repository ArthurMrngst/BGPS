package com.example.bgps.domain.trip

import com.example.bgps.data.database.TripEntity
import com.example.bgps.data.database.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Test

class TripStatsCalculatorTest {

    private fun trip(distanceMeters: Double, vehicleType: VehicleType, startTime: Long) = TripEntity(
        startTime = startTime,
        endTime = startTime + 1_000L,
        distanceMeters = distanceMeters,
        vehicleType = vehicleType
    )

    @Test
    fun `sums distance and groups by vehicle type`() {
        val trips = listOf(
            trip(5_000.0, VehicleType.VEHICLE, 1_000L),
            trip(3_000.0, VehicleType.VEHICLE, 2_000L),
            trip(2_000.0, VehicleType.WALKING, 3_000L)
        )

        val result = TripStatsCalculator.summarize(trips, from = 0L)

        assertEquals(3, result.tripCount)
        assertEquals(10_000.0, result.totalDistanceMeters, 0.001)
        assertEquals(2, result.byVehicle.size)
        assertEquals(VehicleType.VEHICLE, result.byVehicle[0].vehicleType)
        assertEquals(2, result.byVehicle[0].tripCount)
        assertEquals(8_000.0, result.byVehicle[0].totalDistanceMeters, 0.001)
    }

    @Test
    fun `ignores trips before the period start`() {
        val trips = listOf(
            trip(5_000.0, VehicleType.VEHICLE, 1_000L),
            trip(3_000.0, VehicleType.VEHICLE, 5_000L)
        )

        val result = TripStatsCalculator.summarize(trips, from = 2_500L)

        assertEquals(1, result.tripCount)
        assertEquals(3_000.0, result.totalDistanceMeters, 0.001)
    }

    @Test
    fun `empty list gives zero totals`() {
        val result = TripStatsCalculator.summarize(emptyList(), from = 0L)

        assertEquals(0, result.tripCount)
        assertEquals(0.0, result.totalDistanceMeters, 0.001)
        assertEquals(0, result.byVehicle.size)
    }
}
