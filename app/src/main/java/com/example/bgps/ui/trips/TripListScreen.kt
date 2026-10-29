@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.trips

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.bgps.data.database.TripEntity
import com.example.bgps.data.database.VehicleType
import com.example.bgps.location.TripTrackingService
import com.example.bgps.location.hasForegroundLocationPermission
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripListScreen(
    onOpenLocationAccess: () -> Unit,
    viewModel: TripViewModel = hiltViewModel()
) {
    val trips by viewModel.trips.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var locationGranted by remember { mutableStateOf(hasForegroundLocationPermission(context)) }
    // Chỉ là trạng thái lạc quan theo thao tác bấm nút gần nhất, không hỏi
    // lại service đang chạy thật hay không — đủ dùng cho một màn đơn giản,
    // không cần thêm cơ chế đồng bộ trạng thái phức tạp.
    var isTracking by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                locationGranted = hasForegroundLocationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Hành trình") }) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            if (!locationGranted) {
                Card(onClick = onOpenLocationAccess, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cần bật quyền vị trí", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Bấm vào đây để bật trước khi ghi hành trình.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        val intent = Intent(context, TripTrackingService::class.java).apply {
                            if (isTracking) action = TripTrackingService.ACTION_STOP
                        }
                        ContextCompat.startForegroundService(context, intent)
                        isTracking = !isTracking
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isTracking) "Dừng hành trình" else "Bắt đầu hành trình")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (trips.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có hành trình nào")
                }
            } else {
                LazyColumn {
                    items(trips, key = { it.id }) { TripRow(it) }
                }
            }
        }
    }
}

@Composable
private fun TripRow(trip: TripEntity) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val km = trip.distanceMeters / 1000.0

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(vehicleLabel(trip.vehicleType), style = MaterialTheme.typography.titleSmall)
                Text("${"%.1f".format(km)} km (ước tính)")
            }
            Text(dateFormatter.format(Date(trip.startTime)), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun vehicleLabel(type: VehicleType): String = when (type) {
    VehicleType.WALKING -> "Đi bộ"
    VehicleType.CYCLING -> "Xe đạp"
    VehicleType.VEHICLE -> "Xe cơ giới"
    VehicleType.UNKNOWN -> "Không rõ phương tiện"
}
