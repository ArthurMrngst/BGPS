@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.transactions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionType
import com.example.bgps.ui.common.OsmMapView
import org.osmdroid.util.GeoPoint
import java.text.NumberFormat
import java.util.Locale

// Toạ độ trung tâm Hà Nội — chỉ dùng khi chưa có giao dịch nào có vị trí.
private val DEFAULT_CENTER = GeoPoint(21.0285, 105.8542)

// Khác với bản đồ hành trình (vẽ đường đi 1 chuyến), màn này vẽ TẤT CẢ các
// điểm giao dịch đã có toạ độ (xem CurrentLocationProvider) thành marker
// riêng lẻ, không nối thành đường — mục đích là xem chi tiêu đã xảy ra ở
// đâu, không phải đã di chuyển qua đâu.
@Composable
fun SpendingMapScreen(
    viewModel: SpendingMapViewModel = hiltViewModel()
) {
    val transactions by viewModel.locatedTransactions.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Bản đồ chi tiêu") }) }
    ) { innerPadding ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Chưa có giao dịch nào có vị trí") }
        } else {
            val markers = transactions.map { tx ->
                GeoPoint(tx.latitude!!, tx.longitude!!) to markerLabel(tx)
            }
            OsmMapView(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                center = centroid(markers.map { it.first }),
                zoom = 13.0,
                markers = markers
            )
        }
    }
}

private fun centroid(points: List<GeoPoint>): GeoPoint {
    if (points.isEmpty()) return DEFAULT_CENTER
    val avgLat = points.sumOf { it.latitude } / points.size
    val avgLon = points.sumOf { it.longitude } / points.size
    return GeoPoint(avgLat, avgLon)
}

private fun markerLabel(transaction: TransactionEntity): String {
    val sign = if (transaction.type == TransactionType.EXPENSE) "-" else "+"
    val amount = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(transaction.amount)
    val place = transaction.merchant ?: transaction.category
    return "$place · $sign${amount}đ"
}
