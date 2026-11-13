@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.trips

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.example.bgps.domain.trip.FuelCostCalculator
import com.example.bgps.ui.common.OsmMapView
import org.osmdroid.util.GeoPoint
import java.text.NumberFormat
import java.util.Locale

// Toạ độ trung tâm Hà Nội — chỉ dùng làm vị trí camera mặc định khi chưa có
// điểm GPS nào (không nên xảy ra với hành trình hợp lệ, nhưng tránh crash).
private val DEFAULT_CENTER = GeoPoint(21.0285, 105.8542)

@Composable
fun TripDetailScreen(
    onBack: () -> Unit,
    viewModel: TripDetailViewModel = hiltViewModel()
) {
    val trip by viewModel.trip.collectAsState()
    val points by viewModel.points.collectAsState()
    val loaded by viewModel.loaded.collectAsState()
    val fuelSettings by viewModel.fuelSettings.collectAsState()
    val recordState by viewModel.recordFuelExpenseState.collectAsState()

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
                val geoPoints = points.map { GeoPoint(it.latitude, it.longitude) }

                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    OsmMapView(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        center = geoPoints.firstOrNull() ?: DEFAULT_CENTER,
                        routePoints = geoPoints,
                        markers = buildList {
                            geoPoints.firstOrNull()?.let { add(it to "Bắt đầu") }
                            if (geoPoints.size >= 2) add(geoPoints.last() to "Kết thúc")
                        }
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            vehicleLabel(currentTrip.vehicleType),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${"%.2f".format(currentTrip.distanceMeters / 1000.0)} km (ước tính)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        currentTrip.fuelVehicleType?.let { vehicle ->
                            val cost = FuelCostCalculator.estimateCostVnd(
                                distanceMeters = currentTrip.distanceMeters,
                                vehicleType = vehicle,
                                motorbikeLitersPer100Km = fuelSettings.motorbikeLitersPer100Km,
                                carLitersPer100Km = fuelSettings.carLitersPer100Km,
                                pricePerLiterVnd = fuelSettings.pricePerLiterVnd
                            )
                            val currencyFormatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                            Text(
                                "~${currencyFormatter.format(cost)}đ tiền xăng (ước tính)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            when (recordState) {
                                RecordFuelExpenseState.Recorded -> Text(
                                    "Đã ghi vào Giao dịch (danh mục \"Xăng xe\").",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                else -> Button(
                                    onClick = { viewModel.recordFuelExpense() },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) { Text("Ghi thành chi tiêu") }
                            }
                        }
                        if (geoPoints.isEmpty()) {
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

    if (recordState == RecordFuelExpenseState.DuplicateWarning) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRecordFuelExpenseState,
            title = { Text("Đã có khoản xăng gần đây") },
            text = {
                Text("Bạn đã có một giao dịch \"Xăng xe\" gần thời điểm hành trình này rồi. Vẫn ghi thêm?")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.recordFuelExpense(force = true) }) { Text("Vẫn ghi") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRecordFuelExpenseState) { Text("Huỷ") }
            }
        )
    }
}
