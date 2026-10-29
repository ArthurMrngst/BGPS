package com.example.bgps.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// WALKING/CYCLING/VEHICLE là từ Activity Recognition khi có; UNKNOWN khi
// không nhận diện được phương tiện (kể cả bằng tốc độ) hoặc không đủ dữ liệu.
enum class VehicleType { WALKING, CYCLING, VEHICLE, UNKNOWN }

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
    val endLongitude: Double? = null
)
