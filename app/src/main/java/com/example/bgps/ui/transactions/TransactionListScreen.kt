@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.transactions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("BGps") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { innerPadding ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có giao dịch nào")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                items(transactions, key = { it.id }) { tx ->
                    TransactionRow(
                        transaction = tx,
                        onDelete = { viewModel.deleteTransaction(tx) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, type, category, note ->
                viewModel.addTransaction(amount, type, category, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionEntity,
    onDelete: () -> Unit
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val sign = if (isExpense) "-" else "+"
    val amountColor =
        if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    ListItem(
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
