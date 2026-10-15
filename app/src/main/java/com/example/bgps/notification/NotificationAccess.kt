package com.example.bgps.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat

// NotificationListenerService chỉ chạy được sau khi người dùng tự bật quyền
// này trong Cài đặt hệ thống (không xin được bằng runtime permission thông
// thường). Dùng hàm này để biết có cần dẫn người dùng tới màn hướng dẫn hay
// không.
fun isNotificationAccessGranted(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
