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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionType
import com.example.bgps.notification.isNotificationAccessGranted
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionListScreen(
    onOpenSummary: () -> Unit = {},
    onEditTransaction: (String) -> Unit = {},
    onOpenNotificationAccess: () -> Unit = {},
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var notificationAccessGranted by remember { mutableStateOf(isNotificationAccessGranted(context)) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationAccessGranted = isNotificationAccessGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BGps") },
                actions = { TextButton(onClick = onOpenSummary) { Text("Tổng hợp") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (!notificationAccessGranted) {
                NotificationAccessBanner(onClick = onOpenNotificationAccess)
            }
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có giao dịch nào")
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

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, type, category, merchant, note ->
                viewModel.addTransaction(amount, type, category, merchant, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun NotificationAccessBanner(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Tự động ghi giao dịch từ ngân hàng", style = MaterialTheme.typography.titleSmall)
            Text(
                "Bật đọc thông báo để BGps tự nhận giao dịch từ app ngân hàng/ví đã kết nối.",
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
    val amountColor =
        if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

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
