package com.example.bgps.domain.trip

import com.example.bgps.data.database.VehicleType
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class TripSummary(
    val distanceMeters: Double,
    val vehicleType: VehicleType,
    val durationMillis: Long
)

// Tính quãng đường và ước tính loại phương tiện từ danh sách điểm GPS. Hàm
// thuần, không phụ thuộc android.location.Location nên viết test được bằng
// JVM. Chỉ dùng tốc độ trung bình làm phương án DỰ PHÒNG khi không có kết
// quả Activity Recognition — việc nhận diện phương tiện không bao giờ chính
// xác tuyệt đối, kết quả luôn chỉ là ước tính.
object TripCalculator {
    private const val EARTH_RADIUS_METERS = 6_371_000.0

    // Công thức haversine: khoảng cách đường chim bay giữa 2 toạ độ.
    fun distanceMeters(a: TripPoint, b: TripPoint): Double {
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        return 2 * EARTH_RADIUS_METERS * asin(sqrt(h.coerceIn(0.0, 1.0)))
    }

    // Cộng dồn khoảng cách giữa các điểm liên tiếp — không phải khoảng cách
    // điểm đầu tới điểm cuối, vì hành trình thường không phải đường thẳng.
    fun totalDistanceMeters(points: List<TripPoint>): Double {
        if (points.size < 2) return 0.0
        return points.zipWithNext { a, b -> distanceMeters(a, b) }.sum()
    }

    fun classifyBySpeed(points: List<TripPoint>): VehicleType {
        if (points.size < 2) return VehicleType.UNKNOWN
        val distance = totalDistanceMeters(points)
        val durationSeconds = (points.last().timestampMillis - points.first().timestampMillis) / 1000.0
        if (durationSeconds <= 0) return VehicleType.UNKNOWN
        val speedKmh = (distance / durationSeconds) * 3.6
        return when {
            speedKmh < 7.0 -> VehicleType.WALKING
            speedKmh < 25.0 -> VehicleType.CYCLING
            else -> VehicleType.VEHICLE
        }
    }

    // activityHint: kết quả từ Activity Recognition nếu có — ưu tiên hơn
    // phân loại theo tốc độ vì nó dựa trên cảm biến thật, không chỉ suy ra
    // từ 2 điểm đầu/cuối.
    fun summarize(points: List<TripPoint>, activityHint: VehicleType? = null): TripSummary {
        val distance = totalDistanceMeters(points)
        val duration = if (points.size >= 2) {
            points.last().timestampMillis - points.first().timestampMillis
        } else {
            0L
        }
        val vehicle = activityHint ?: classifyBySpeed(points)
        return TripSummary(distance, vehicle, duration)
    }
}
