package com.example.bgps.notification

import android.content.Context
import android.os.PowerManager

// Doze/App Standby có thể "ngủ đông" tiến trình app, kể cả khi
// NotificationListenerService đã được cấp quyền — hay gặp nhất trên các máy
// Samsung/Xiaomi/Oppo có trình quản lý pin riêng ngoài Android gốc. Việc
// được đưa vào danh sách "bỏ qua tối ưu hoá pin" giúp giảm khả năng bị dừng.
fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}
