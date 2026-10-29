package com.example.bgps.domain.trip

// Một điểm GPS ghi được trong lúc theo dõi hành trình.
data class TripPoint(
    val latitude: Double,
    val longitude: Double,
    val timestampMillis: Long
)
