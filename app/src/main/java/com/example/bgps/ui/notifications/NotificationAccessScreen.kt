@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.notifications

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.bgps.notification.isIgnoringBatteryOptimizations
import com.example.bgps.notification.isNotificationAccessGranted

@Composable
fun NotificationAccessScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var notificationGranted by remember { mutableStateOf(isNotificationAccessGranted(context)) }
    var batteryExempt by remember { mutableStateOf(isIgnoringBatteryOptimizations(context)) }

    // Cả hai trạng thái này chỉ đổi được qua màn Cài đặt hệ thống, không phải
    // dialog xin quyền trong app, nên phải tự kiểm tra lại mỗi lần quay về.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationGranted = isNotificationAccessGranted(context)
                batteryExempt = isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kết nối ngân hàng") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Quay lại") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (notificationGranted) "Đã bật đọc thông báo" else "Chưa bật đọc thông báo",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "BGps cần quyền đọc thông báo để tự nhận diện giao dịch từ các app " +
                    "ngân hàng/ví đã kết nối (danh sách whitelist cố định, không đọc " +
                    "thông báo của app khác). Chỉ số tiền, loại giao dịch, nơi giao " +
                    "dịch và giờ được rút ra và lưu lại — nội dung thông báo gốc " +
                    "không rời khỏi máy và không được lưu lại."
            )
            Text(
                "Bước 1: bấm nút bên dưới, tìm \"BGps\" trong danh sách rồi bật lên."
            )
            Button(onClick = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }) {
                Text("Mở cài đặt quyền thông báo")
            }

            HorizontalDivider()

            Text(
                text = if (batteryExempt) "Đã được bỏ qua tối ưu hoá pin" else "Chưa được bỏ qua tối ưu hoá pin",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Nếu đã bật ở Bước 1 mà vẫn không nhận giao dịch khi tắt màn hình/" +
                    "đóng app, nguyên nhân thường là hệ điều hành tự tắt app để tiết " +
                    "kiệm pin. Bước 2: bấm nút bên dưới và chọn \"Cho phép\"/\"Allow\"."
            )
            Button(onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:${context.packageName}")
                    )
                )
            }) {
                Text("Bỏ qua tối ưu hoá pin")
            }
            Text(
                "Riêng máy Samsung: mục này đôi khi chưa đủ vì Samsung có danh sách " +
                    "\"Sleeping apps\"/\"Deep sleeping apps\" riêng trong Device Care. " +
                    "Bước 3 (nếu Bước 2 vẫn không đủ): Cài đặt > Ứng dụng > BGps > Pin " +
                    "(Battery), chọn \"Không giới hạn\" (Unrestricted); sau đó mở Device " +
                    "care > Pin > Giới hạn sử dụng nền, gỡ BGps khỏi danh sách app ngủ " +
                    "nếu có."
            )
            OutlinedButton(onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${context.packageName}")
                    )
                )
            }) {
                Text("Mở thông tin ứng dụng")
            }
        }
    }
}
