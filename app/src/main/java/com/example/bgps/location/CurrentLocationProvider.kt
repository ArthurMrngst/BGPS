package com.example.bgps.location

import android.content.Context
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

// Lấy vị trí gần nhất mà hệ thống đã biết (lastLocation) để gắn vào giao
// dịch mới tạo (nhập tay, quét hoá đơn, hoặc từ thông báo ngân hàng) — không
// chủ động bật GPS xin toạ độ mới (tốn pin, phải chờ), chỉ đọc toạ độ có sẵn
// trong bộ nhớ hệ thống. Không có quyền hoặc hệ thống chưa có toạ độ nào thì
// trả về null — giao dịch vẫn lưu bình thường không có vị trí.
@Singleton
class CurrentLocationProvider @Inject constructor(@ApplicationContext private val context: Context) {

    // requireBackgroundPermission = true khi gọi từ một ngữ cảnh chạy nền
    // (BankNotificationListenerService) — Android 10+ chặn vị trí cho app
    // chạy nền nếu chỉ có quyền vị trí thường, cần ACCESS_BACKGROUND_LOCATION.
    // Gọi từ UI đang ở foreground (nhập tay, xác nhận hoá đơn) thì chỉ cần
    // quyền vị trí thường.
    suspend fun getLastLocation(requireBackgroundPermission: Boolean = false): Pair<Double, Double>? {
        val hasPermission = if (requireBackgroundPermission) {
            hasBackgroundLocationPermission(context)
        } else {
            hasForegroundLocationPermission(context)
        }
        if (!hasPermission) return null

        return try {
            suspendCancellableCoroutine { continuation ->
                LocationServices.getFusedLocationProviderClient(context).lastLocation
                    .addOnSuccessListener { location ->
                        val result = location?.let { it.latitude to it.longitude }
                        if (continuation.isActive) continuation.resume(result)
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
            }
        } catch (e: SecurityException) {
            null
        }
    }
}
