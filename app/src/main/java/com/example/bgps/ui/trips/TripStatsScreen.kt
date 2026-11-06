@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.bgps.domain.SummaryPeriod
import com.example.bgps.domain.trip.VehicleStats

// Thống kê hành trình (F2): tổng km + số chuyến theo tuần/tháng, chia theo
// phương tiện — hoàn toàn dựa trên dữ liệu Trip đã ghi được, không cần đo gì
// thêm.
@Composable
fun TripStatsScreen(
    onBack: () -> Unit,
    viewModel: TripStatsViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val period by viewModel.period.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê di chuyển") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Quay lại") } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val periods = listOf(SummaryPeriod.WEEK to "Tuần này", SummaryPeriod.MONTH to "Tháng này")
            val selectedIndex = periods.indexOfFirst { it.first == period }.coerceAtLeast(0)
            TabRow(selectedTabIndex = selectedIndex) {
                periods.forEach { (periodValue, label) ->
                    Tab(
                        selected = periodValue == period,
                        onClick = { viewModel.setPeriod(periodValue) },
                        text = { Text(label) }
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Tổng quãng đường", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "${"%.1f".format(stats.totalDistanceMeters / 1000.0)} km",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text("${stats.tripCount} chuyến đi", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (stats.byVehicle.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có hành trình nào trong kỳ này")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    items(stats.byVehicle, key = { it.vehicleType }) { VehicleStatsRow(it) }
                }
            }
        }
    }
}

@Composable
private fun VehicleStatsRow(item: VehicleStats) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(vehicleLabel(item.vehicleType), style = MaterialTheme.typography.bodyMedium)
        Text(
            "${"%.1f".format(item.totalDistanceMeters / 1000.0)} km — ${item.tripCount} chuyến",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
