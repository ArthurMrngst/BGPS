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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.bgps.data.database.FuelVehicleType
import com.example.bgps.data.database.TripEntity
import com.example.bgps.data.database.VehicleType
import com.example.bgps.domain.trip.FuelCostCalculator
import com.example.bgps.location.FuelSettings
import com.example.bgps.location.TripTrackingService
import com.example.bgps.location.hasForegroundLocationPermission
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripListScreen(
    onOpenLocationAccess: () -> Unit,
    onOpenTripDetail: (String) -> Unit = {},
    onOpenLiveTrip: () -> Unit = {},
    onOpenFuelSettings: () -> Unit = {},
    onOpenTripStats: () -> Unit = {},
    viewModel: TripViewModel = hiltViewModel()
) {
    val trips by viewModel.trips.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val fuelSettings by viewModel.fuelSettings.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var locationGranted by remember { mutableStateOf(hasForegroundLocationPermission(context)) }
    // Activity Recognition chỉ biết "đang ở trong xe" nói chung, không tách
    // được xe máy/ô tô — để người dùng chọn thủ công trước khi bắt đầu, chỉ
    // dùng cho tính tiền xăng ước tính (không chọn thì không tính xăng).
    var selectedFuelVehicle by remember { mutableStateOf<FuelVehicleType?>(null) }

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
        topBar = {
            TopAppBar(
                title = { Text("Hành trình") },
                actions = {
                    TextButton(onClick = onOpenTripStats) { Text("Thống kê") }
                    TextButton(onClick = onOpenFuelSettings) { Text("Cài đặt xăng") }
                }
            )
        }
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
            } else if (isTracking) {
                Button(onClick = onOpenLiveTrip, modifier = Modifier.fillMaxWidth()) {
                    Text("Đang ghi hành trình — xem bản đồ")
                }
            } else {
                Text("Phương tiện (để tính tiền xăng)", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)) {
                    FilterChip(
                        selected = selectedFuelVehicle == FuelVehicleType.MOTORBIKE,
                        onClick = {
                            selectedFuelVehicle = if (selectedFuelVehicle == FuelVehicleType.MOTORBIKE) {
                                null
                            } else {
                                FuelVehicleType.MOTORBIKE
                            }
                        },
                        label = { Text("Xe máy") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = selectedFuelVehicle == FuelVehicleType.CAR,
                        onClick = {
                            selectedFuelVehicle = if (selectedFuelVehicle == FuelVehicleType.CAR) {
                                null
                            } else {
                                FuelVehicleType.CAR
                            }
                        },
                        label = { Text("Ô tô") }
                    )
                }
                Button(
                    onClick = {
                        val intent = Intent(context, TripTrackingService::class.java).apply {
                            putExtra(TripTrackingService.EXTRA_FUEL_VEHICLE_TYPE, selectedFuelVehicle?.name)
                        }
                        ContextCompat.startForegroundService(context, intent)
                        onOpenLiveTrip()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Bắt đầu hành trình")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (trips.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có hành trình nào")
                }
            } else {
                LazyColumn {
                    items(trips, key = { it.id }) { trip ->
                        TripRow(trip, fuelSettings, onClick = { onOpenTripDetail(trip.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TripRow(trip: TripEntity, fuelSettings: FuelSettings, onClick: () -> Unit) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }
    val km = trip.distanceMeters / 1000.0

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(vehicleLabel(trip.vehicleType), style = MaterialTheme.typography.titleSmall)
                Text("${"%.1f".format(km)} km (ước tính)")
            }
            Text(dateFormatter.format(Date(trip.startTime)), style = MaterialTheme.typography.bodySmall)
            trip.fuelVehicleType?.let { vehicle ->
                val cost = FuelCostCalculator.estimateCostVnd(
                    distanceMeters = trip.distanceMeters,
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
        }
    }
}

fun vehicleLabel(type: VehicleType): String = when (type) {
    VehicleType.WALKING -> "Đi bộ"
    VehicleType.CYCLING -> "Xe đạp"
    VehicleType.VEHICLE -> "Xe cơ giới"
    VehicleType.UNKNOWN -> "Không rõ phương tiện"
}
