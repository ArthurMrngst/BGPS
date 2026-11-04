@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.trips

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bgps.domain.trip.FuelCostCalculator
import com.example.bgps.domain.trip.TripCalculator
import com.example.bgps.location.TripTrackingService
import com.example.bgps.ui.common.OsmMapView
import org.osmdroid.util.GeoPoint
import java.text.NumberFormat
import java.util.Locale

// Toạ độ trung tâm Hà Nội — chỉ dùng làm vị trí camera mặc định trước khi
// nhận được điểm GPS đầu tiên.
private val DEFAULT_CENTER = GeoPoint(21.0285, 105.8542)

// Màn bản đồ hiện khi một hành trình đang được ghi: quan sát LiveTripTracker
// (qua Service, không qua DB vì trip_points chỉ có sau khi dừng) để vẽ lại
// đường đi và tự dời camera theo vị trí mới nhất mỗi khi có toạ độ mới.
@Composable
fun LiveTripScreen(
    onStop: () -> Unit,
    viewModel: LiveTripViewModel = hiltViewModel()
) {
    val points by viewModel.points.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val fuelVehicleType by viewModel.fuelVehicleType.collectAsState()
    val fuelSettings by viewModel.fuelSettings.collectAsState()
    val context = LocalContext.current
    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }

    LaunchedEffect(isTracking) {
        if (!isTracking) onStop()
    }

    val geoPoints = points.map { GeoPoint(it.latitude, it.longitude) }
    val current = geoPoints.lastOrNull()
    val distanceMeters = TripCalculator.totalDistanceMeters(points)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Đang ghi hành trình") }) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OsmMapView(
                modifier = Modifier.fillMaxWidth().weight(1f),
                center = current ?: DEFAULT_CENTER,
                routePoints = geoPoints,
                markers = current?.let { listOf(it to "Vị trí hiện tại") } ?: emptyList()
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "${"%.2f".format(distanceMeters / 1000.0)} km — ${geoPoints.size} điểm đã ghi",
                    style = MaterialTheme.typography.bodyMedium
                )
                fuelVehicleType?.let { vehicle ->
                    val cost = FuelCostCalculator.estimateCostVnd(
                        distanceMeters = distanceMeters,
                        vehicleType = vehicle,
                        motorbikeLitersPer100Km = fuelSettings.motorbikeLitersPer100Km,
                        carLitersPer100Km = fuelSettings.carLitersPer100Km,
                        pricePerLiterVnd = fuelSettings.pricePerLiterVnd
                    )
                    Text(
                        "~${currencyFormatter.format(cost)}đ tiền xăng (ước tính)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(
                    onClick = {
                        val intent = Intent(context, TripTrackingService::class.java).apply {
                            action = TripTrackingService.ACTION_STOP
                        }
                        ContextCompat.startForegroundService(context, intent)
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Dừng hành trình")
                }
            }
        }
    }
}
