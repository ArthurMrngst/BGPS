@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.notifications

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.example.bgps.notification.isNotificationAccessGranted

@Composable
fun NotificationAccessScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(isNotificationAccessGranted(context)) }

    // Người dùng bật quyền trong màn Cài đặt rồi quay lại app (không phải
    // runtime permission dialog), nên phải tự kiểm tra lại mỗi lần màn này
    // resume thay vì chỉ đọc một lần lúc vào màn.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = isNotificationAccessGranted(context)
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
                text = if (granted) "Đã bật đọc thông báo" else "Chưa bật đọc thông báo",
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
                "Bước tiếp theo: bấm nút bên dưới, tìm \"BGps\" trong danh sách rồi bật lên."
            )
            Button(onClick = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }) {
                Text("Mở cài đặt quyền thông báo")
            }
        }
    }
}
