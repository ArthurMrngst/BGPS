package com.example.bgps.domain.trip

import com.example.bgps.data.database.FuelVehicleType
import kotlin.math.roundToLong

// Ước tính tiền xăng từ quãng đường. Định mức tiêu hao và giá xăng đều nhận
// qua tham số (người dùng chỉnh được ở màn Cài đặt xăng — xem
// FuelSettingsStore); DEFAULT_* chỉ là số tham khảo dùng khi người dùng chưa
// tự chỉnh, KHÔNG phải giá/tiêu hao thật của xe cụ thể nào.
object FuelCostCalculator {
    const val DEFAULT_MOTORBIKE_LITERS_PER_100KM = 2.1
    const val DEFAULT_CAR_LITERS_PER_100KM = 7.0
    const val DEFAULT_PRICE_PER_LITER_VND = 20_500.0

    fun estimateCostVnd(
        distanceMeters: Double,
        vehicleType: FuelVehicleType,
        motorbikeLitersPer100Km: Double = DEFAULT_MOTORBIKE_LITERS_PER_100KM,
        carLitersPer100Km: Double = DEFAULT_CAR_LITERS_PER_100KM,
        pricePerLiterVnd: Double = DEFAULT_PRICE_PER_LITER_VND
    ): Long {
        val distanceKm = distanceMeters / 1000.0
        val lPer100 = when (vehicleType) {
            FuelVehicleType.MOTORBIKE -> motorbikeLitersPer100Km
            FuelVehicleType.CAR -> carLitersPer100Km
        }
        val liters = (distanceKm / 100.0) * lPer100
        return (liters * pricePerLiterVnd).roundToLong()
    }
}
