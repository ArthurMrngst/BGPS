package com.example.bgps.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// WALKING/CYCLING/VEHICLE là từ Activity Recognition khi có; UNKNOWN khi
// không nhận diện được phương tiện (kể cả bằng tốc độ) hoặc không đủ dữ liệu.
enum class VehicleType { WALKING, CYCLING, VEHICLE, UNKNOWN }

// Activity Recognition chỉ phân biệt được "đang ở trong xe" nói chung
// (VehicleType.VEHICLE), không tách được xe máy với ô tô — nên đây là lựa
// chọn THỦ CÔNG của người dùng lúc bắt đầu hành trình, chỉ dùng để tính tiền
// xăng ước tính (xem FuelCostCalculator), không ảnh hưởng tới VehicleType.
enum class FuelVehicleType { MOTORBIKE, CAR }

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val startTime: Long,
    val endTime: Long,
    val distanceMeters: Double,
    val vehicleType: VehicleType,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val endLatitude: Double? = null,
    val endLongitude: Double? = null,
    val fuelVehicleType: FuelVehicleType? = null
)
