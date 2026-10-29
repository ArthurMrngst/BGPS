package com.example.bgps.location

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Lưu điểm vị trí gần nhất mà app biết được (TripTrackingService cập nhật
// liên tục trong lúc đang ghi hành trình). Dùng để gắn vào giao dịch tạo từ
// thông báo ngân hàng (BankNotificationListenerService) mà không cần xin
// quyền vị trí nền riêng cho việc đó — đúng tinh thần ban đầu: "chưa có vị
// trí thì để null, giao dịch vẫn lưu bình thường".
@Singleton
class LastKnownLocationStore @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("last_location", Context.MODE_PRIVATE)

    fun save(latitude: Double, longitude: Double) {
        prefs.edit()
            .putLong(KEY_LAT, java.lang.Double.doubleToLongBits(latitude))
            .putLong(KEY_LNG, java.lang.Double.doubleToLongBits(longitude))
            .putLong(KEY_TIME, System.currentTimeMillis())
            .apply()
    }

    // null nếu chưa từng ghi được vị trí, hoặc điểm đã quá cũ — tránh gắn
    // nhầm vị trí của một chuyến đi cũ vào giao dịch mới không liên quan.
    fun getRecent(maxAgeMillis: Long = DEFAULT_MAX_AGE_MILLIS): Pair<Double, Double>? {
        val time = prefs.getLong(KEY_TIME, 0L)
        if (time == 0L || System.currentTimeMillis() - time > maxAgeMillis) return null
        val latBits = prefs.getLong(KEY_LAT, 0L)
        val lngBits = prefs.getLong(KEY_LNG, 0L)
        return java.lang.Double.longBitsToDouble(latBits) to java.lang.Double.longBitsToDouble(lngBits)
    }

    companion object {
        private const val KEY_LAT = "lat"
        private const val KEY_LNG = "lng"
        private const val KEY_TIME = "time"
        private const val DEFAULT_MAX_AGE_MILLIS = 60 * 60 * 1000L // 1 giờ
    }
}
