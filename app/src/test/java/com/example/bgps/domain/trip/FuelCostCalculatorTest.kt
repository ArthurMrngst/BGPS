package com.example.bgps.domain.trip

import com.example.bgps.data.database.FuelVehicleType
import org.junit.Assert.assertEquals
import org.junit.Test

class FuelCostCalculatorTest {

    @Test
    fun `estimates motorbike cost for 100km at default price`() {
        val cost = FuelCostCalculator.estimateCostVnd(100_000.0, FuelVehicleType.MOTORBIKE)

        // 2.1L * 20,500đ = 43,050đ
        assertEquals(43_050L, cost)
    }

    @Test
    fun `estimates car cost for 100km at default price`() {
        val cost = FuelCostCalculator.estimateCostVnd(100_000.0, FuelVehicleType.CAR)

        // 7L * 20,500đ = 143,500đ
        assertEquals(143_500L, cost)
    }

    @Test
    fun `car costs more than motorbike for the same distance`() {
        val motorbikeCost = FuelCostCalculator.estimateCostVnd(10_000.0, FuelVehicleType.MOTORBIKE)
        val carCost = FuelCostCalculator.estimateCostVnd(10_000.0, FuelVehicleType.CAR)

        assert(carCost > motorbikeCost)
    }

    @Test
    fun `zero distance costs nothing`() {
        val cost = FuelCostCalculator.estimateCostVnd(0.0, FuelVehicleType.CAR)

        assertEquals(0L, cost)
    }

    @Test
    fun `respects a custom price per liter`() {
        val cost = FuelCostCalculator.estimateCostVnd(
            distanceMeters = 100_000.0,
            vehicleType = FuelVehicleType.MOTORBIKE,
            pricePerLiterVnd = 10_000.0
        )

        assertEquals(21_000L, cost)
    }
}
