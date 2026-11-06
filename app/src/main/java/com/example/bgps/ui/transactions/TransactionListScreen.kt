@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionSource
import com.example.bgps.data.database.TransactionType
import com.example.bgps.domain.DateRangePreset
import com.example.bgps.domain.TransactionFilter
import com.example.bgps.notification.isIgnoringBatteryOptimizations
import com.example.bgps.notification.isNotificationAccessGranted
import com.example.bgps.ui.theme.ExpenseOrange
import com.example.bgps.ui.theme.IncomeBlue
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionListScreen(
    onEditTransaction: (String) -> Unit = {},
    onOpenNotificationAccess: () -> Unit = {},
    onOpenTrips: () -> Unit = {},
    onOpenSpendingMap: () -> Unit = {},
    onOpenCategoryManage: () -> Unit = {},
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val categoryNames by viewModel.categoryNames.collectAsState()
    var filtersExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var notificationAccessGranted by remember { mutableStateOf(isNotificationAccessGranted(context)) }
    var batteryExempt by remember { mutableStateOf(isIgnoringBatteryOptimizations(context)) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationAccessGranted = isNotificationAccessGranted(context)
                batteryExempt = isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BGps") },
                actions = {
                    TextButton(onClick = { filtersExpanded = !filtersExpanded }) {
                        Text(if (filter.isActive) "Lọc (đang bật)" else "Lọc")
                    }
                    TextButton(onClick = onOpenCategoryManage) { Text("Danh mục") }
                    TextButton(onClick = onOpenSpendingMap) { Text("Bản đồ") }
                    TextButton(onClick = onOpenTrips) { Text("Hành trình") }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (!notificationAccessGranted || !batteryExempt) {
                NotificationAccessBanner(
                    batteryStepNeeded = notificationAccessGranted && !batteryExempt,
                    onClick = onOpenNotificationAccess
                )
            }
            OutlinedTextField(
                value = filter.keyword,
                onValueChange = { viewModel.setFilter(filter.copy(keyword = it)) },
                label = { Text("Tìm theo nơi giao dịch, ghi chú, danh mục") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (filtersExpanded) {
                FilterPanel(filter = filter, categoryNames = categoryNames, onFilterChange = viewModel::setFilter)
            }
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (filter.isActive) "Không có giao dịch nào khớp bộ lọc" else "Chưa có giao dịch nào")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxSize()) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionRow(
                            transaction = tx,
                            onClick = { onEditTransaction(tx.id) },
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPanel(
    filter: TransactionFilter,
    categoryNames: List<String>,
    onFilterChange: (TransactionFilter) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("Danh mục", style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = filter.category == null,
                    onClick = { onFilterChange(filter.copy(category = null)) },
                    label = { Text("Tất cả") }
                )
            }
            items(categoryNames) { name ->
                FilterChip(
                    selected = filter.category == name,
                    onClick = { onFilterChange(filter.copy(category = name)) },
                    label = { Text(name) }
                )
            }
        }

        Text("Nguồn", style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = filter.source == null,
                    onClick = { onFilterChange(filter.copy(source = null)) },
                    label = { Text("Tất cả") }
                )
            }
            items(TransactionSource.entries.toList()) { source ->
                FilterChip(
                    selected = filter.source == source,
                    onClick = { onFilterChange(filter.copy(source = source)) },
                    label = { Text(sourceLabel(source)) }
                )
            }
        }

        Text("Thời gian", style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(DateRangePreset.entries.toList()) { preset ->
                FilterChip(
                    selected = filter.dateRange == preset,
                    onClick = { onFilterChange(filter.copy(dateRange = preset)) },
                    label = { Text(dateRangeLabel(preset)) }
                )
            }
        }

        if (filter.isActive) {
            TextButton(onClick = { onFilterChange(TransactionFilter()) }) { Text("Xoá bộ lọc") }
        }
    }
}

private fun sourceLabel(source: TransactionSource): String = when (source) {
    TransactionSource.MANUAL -> "Nhập tay"
    TransactionSource.NOTIFICATION -> "Thông báo ngân hàng"
    TransactionSource.OCR -> "Quét hoá đơn"
    TransactionSource.GPS -> "GPS"
}

private fun dateRangeLabel(preset: DateRangePreset): String = when (preset) {
    DateRangePreset.ALL -> "Tất cả thời gian"
    DateRangePreset.LAST_7_DAYS -> "7 ngày qua"
    DateRangePreset.LAST_30_DAYS -> "30 ngày qua"
    DateRangePreset.THIS_MONTH -> "Tháng này"
}

@Composable
private fun NotificationAccessBanner(batteryStepNeeded: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Tự động ghi giao dịch từ ngân hàng", style = MaterialTheme.typography.titleSmall)
            Text(
                if (batteryStepNeeded) {
                    "Đã bật đọc thông báo, nhưng máy có thể tự tắt app khi tắt màn " +
                        "hình. Bấm vào đây để bỏ qua tối ưu hoá pin cho BGps."
                } else {
                    "Bật đọc thông báo để BGps tự nhận giao dịch từ app ngân hàng/ví đã kết nối."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val sign = if (isExpense) "-" else "+"
    val amountColor = if (isExpense) ExpenseOrange else IncomeBlue

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(transaction.category) },
        supportingContent = {
            Text(
                listOfNotNull(transaction.note, formatTime(transaction.timestamp))
                    .joinToString(" · ")
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$sign${formatVnd(transaction.amount)}",
                    color = amountColor,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onDelete) { Text("Xoá") }
            }
        }
    )
}

private fun formatVnd(amount: Long): String =
    NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(amount) + " ₫"

private fun formatTime(timestamp: Long): String =
    SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(timestamp))
