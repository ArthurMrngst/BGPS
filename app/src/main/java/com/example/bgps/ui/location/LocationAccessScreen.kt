@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.location

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.example.bgps.location.hasActivityRecognitionPermission
import com.example.bgps.location.hasBackgroundLocationPermission
import com.example.bgps.location.hasForegroundLocationPermission

@Composable
fun LocationAccessScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var foregroundGranted by remember { mutableStateOf(hasForegroundLocationPermission(context)) }
    var backgroundGranted by remember { mutableStateOf(hasBackgroundLocationPermission(context)) }
    var activityGranted by remember { mutableStateOf(hasActivityRecognitionPermission(context)) }

    fun refresh() {
        foregroundGranted = hasForegroundLocationPermission(context)
        backgroundGranted = hasBackgroundLocationPermission(context)
        activityGranted = hasActivityRecognitionPermission(context)
    }

    // Cả 3 quyền chỉ đổi được qua dialog hệ thống hoặc màn Cài đặt, không
    // phải thứ app tự biết ngay lập tức — kiểm tra lại mỗi lần quay về màn.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val foregroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { refresh() }

    val backgroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refresh() }

    val activityLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ghi hành trình") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Quay lại") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "BGps ghi lại quãng đường và loại phương tiện khi bạn chủ động " +
                    "bấm bắt đầu hành trình. Vị trí chỉ lưu trên máy, không tự " +
                    "động gửi lên đâu cả, và bạn tắt được bất cứ lúc nào.",
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider()

            Text(
                text = if (foregroundGranted) "Bước 1: Đã bật vị trí" else "Bước 1: Chưa bật vị trí",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Cần để ghi vị trí trong lúc đang theo dõi một hành trình.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = {
                val permissions = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions += Manifest.permission.POST_NOTIFICATIONS
                }
                foregroundLauncher.launch(permissions.toTypedArray())
            }) { Text("Bật vị trí") }

            HorizontalDivider()

            Text(
                text = if (backgroundGranted) "Bước 2: Đã bật vị trí nền" else "Bước 2: Chưa bật vị trí nền",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Chỉ dùng để gắn toạ độ gần nhất vào giao dịch tự nhận từ " +
                    "thông báo ngân hàng lúc không có hành trình nào đang ghi. " +
                    "Nếu bỏ qua bước này, giao dịch vẫn lưu bình thường, chỉ là " +
                    "không có vị trí đính kèm.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                enabled = foregroundGranted,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // Android 11+: hệ thống không cho gộp xin vị trí nền
                        // chung dialog với vị trí thường nữa, phải qua Cài đặt.
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                }
            ) { Text(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) "Mở Cài đặt" else "Bật vị trí nền") }

            HorizontalDivider()

            Text(
                text = if (activityGranted) "Bước 3: Đã bật nhận diện hoạt động" else "Bước 3: Chưa bật nhận diện hoạt động",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Giúp đoán loại phương tiện (đi bộ/xe đạp/xe cơ giới) chính xác " +
                    "hơn là chỉ tính theo tốc độ trung bình.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    activityLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            }) { Text("Bật nhận diện hoạt động") }
        }
    }
}
