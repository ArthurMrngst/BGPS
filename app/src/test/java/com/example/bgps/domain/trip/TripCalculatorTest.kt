package com.example.bgps.domain.trip

import com.example.bgps.data.database.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Test

class TripCalculatorTest {

    // 0.0005 độ vĩ tuyến ~ 55.6m ở bất kỳ kinh độ nào — dùng để dựng các mốc
    // thời gian cho ra tốc độ mong muốn mà không cần tính tay quãng đường.
    private fun point(latOffset: Double, atMillis: Long) =
        TripPoint(latitude = 21.0 + latOffset, longitude = 105.8, timestampMillis = atMillis)

    @Test
    fun `distanceMeters matches a known reference distance`() {
        // 1 độ kinh tuyến ở xích đạo xấp xỉ 111.2km theo công thức haversine.
        val a = TripPoint(0.0, 0.0, 0)
        val b = TripPoint(0.0, 1.0, 0)

        val distance = TripCalculator.distanceMeters(a, b)

        assertEquals(111_195.0, distance, 500.0)
    }

    @Test
    fun `totalDistanceMeters sums consecutive segments, not endpoint to endpoint`() {
        val points = listOf(
            point(0.0, 0),
            point(0.0005, 60_000),
            point(0.0, 120_000) // quay lại điểm đầu — đường đi vòng, không phải thẳng
        )

        val total = TripCalculator.totalDistanceMeters(points)

        // Hai chặng cộng lại phải lớn hơn hẳn khoảng cách điểm đầu-cuối (gần 0)
        assertEquals(111.3, total, 5.0)
    }

    @Test
    fun `classifies walking speed`() {
        val points = listOf(point(0.0, 0), point(0.0005, 60_000))

        assertEquals(VehicleType.WALKING, TripCalculator.classifyBySpeed(points))
    }

    @Test
    fun `classifies cycling speed`() {
        val points = listOf(point(0.0, 0), point(0.0005, 10_000))

        assertEquals(VehicleType.CYCLING, TripCalculator.classifyBySpeed(points))
    }

    @Test
    fun `classifies vehicle speed`() {
        val points = listOf(point(0.0, 0), point(0.0005, 2_000))

        assertEquals(VehicleType.VEHICLE, TripCalculator.classifyBySpeed(points))
    }

    @Test
    fun `returns unknown when fewer than two points`() {
        assertEquals(VehicleType.UNKNOWN, TripCalculator.classifyBySpeed(listOf(point(0.0, 0))))
        assertEquals(VehicleType.UNKNOWN, TripCalculator.classifyBySpeed(emptyList()))
    }

    @Test
    fun `summarize prefers activity hint over speed-based classification`() {
        // Tốc độ gợi ý WALKING nhưng Activity Recognition báo ON_BICYCLE —
        // kết quả Activity Recognition phải thắng vì dựa trên cảm biến thật.
        val points = listOf(point(0.0, 0), point(0.0005, 60_000))

        val summary = TripCalculator.summarize(points, activityHint = VehicleType.CYCLING)

        assertEquals(VehicleType.CYCLING, summary.vehicleType)
    }

    @Test
    fun `summarize falls back to speed classification without an activity hint`() {
        val points = listOf(point(0.0, 0), point(0.0005, 2_000))

        val summary = TripCalculator.summarize(points)

        assertEquals(VehicleType.VEHICLE, summary.vehicleType)
        assertEquals(2_000L, summary.durationMillis)
    }
}
