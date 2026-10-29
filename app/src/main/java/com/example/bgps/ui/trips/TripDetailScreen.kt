@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.trips

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

// Toạ độ trung tâm Hà Nội — chỉ dùng làm vị trí camera mặc định khi chưa có
// điểm GPS nào (không nên xảy ra với hành trình hợp lệ, nhưng tránh crash).
private val DEFAULT_CENTER = LatLng(21.0285, 105.8542)

@Composable
fun TripDetailScreen(
    onBack: () -> Unit,
    viewModel: TripDetailViewModel = hiltViewModel()
) {
    val trip by viewModel.trip.collectAsState()
    val points by viewModel.points.collectAsState()
    val loaded by viewModel.loaded.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết hành trình") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Quay lại") } }
            )
        }
    ) { innerPadding ->
        val currentTrip = trip
        when {
            !loaded -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Đang tải...") }

            currentTrip == null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Không tìm thấy hành trình") }

            else -> {
                val latLngPoints = points.map { LatLng(it.latitude, it.longitude) }
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        latLngPoints.firstOrNull() ?: DEFAULT_CENTER,
                        16f
                    )
                }

                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        cameraPositionState = cameraPositionState
                    ) {
                        if (latLngPoints.size >= 2) {
                            Polyline(points = latLngPoints, color = MaterialTheme.colorScheme.primary)
                        }
                        latLngPoints.firstOrNull()?.let {
                            Marker(state = MarkerState(position = it), title = "Bắt đầu")
                        }
                        if (latLngPoints.size >= 2) {
                            Marker(state = MarkerState(position = latLngPoints.last()), title = "Kết thúc")
                        }
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            vehicleLabel(currentTrip.vehicleType),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${"%.2f".format(currentTrip.distanceMeters / 1000.0)} km (ước tính)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (latLngPoints.isEmpty()) {
                            Text(
                                "Không có dữ liệu đường đi chi tiết cho hành trình này.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
