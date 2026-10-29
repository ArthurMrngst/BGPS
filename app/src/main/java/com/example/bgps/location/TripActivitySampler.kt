package com.example.bgps.location

import javax.inject.Inject
import javax.inject.Singleton

// Gom các kết quả Activity Recognition nhận được qua broadcast trong lúc
// đang ghi hành trình. TripActivityReceiver ghi vào đây; TripTrackingService
// đọc ra khi kết thúc chuyến đi để chọn loại phương tiện phổ biến nhất.
@Singleton
class TripActivitySampler @Inject constructor() {
    private val samples = mutableListOf<Int>()

    @Synchronized
    fun add(activityType: Int) {
        samples.add(activityType)
    }

    @Synchronized
    fun drain(): List<Int> {
        val copy = samples.toList()
        samples.clear()
        return copy
    }
}
