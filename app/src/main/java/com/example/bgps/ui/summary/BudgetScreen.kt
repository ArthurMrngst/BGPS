@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

// Đặt hạn mức chi tiêu hàng tháng cho từng danh mục (D1) — để trống hoặc 0
// nghĩa là không theo dõi ngân sách cho danh mục đó. Danh sách danh mục giờ
// đọc động từ CategoryRepository (B3 phần 2), không còn enum cố định.
@Composable
fun BudgetScreen(
    onBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val budgets by viewModel.budgets.collectAsState()
    val categoryNames by viewModel.categoryNames.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ngân sách hàng tháng") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Quay lại") } }
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            items(categoryNames, key = { it }) { category ->
                val currentLimit = budgets.find { it.category == category }?.monthlyLimit
                BudgetRow(
                    category = category,
                    currentLimit = currentLimit,
                    onSave = { limit -> viewModel.save(category, limit) }
                )
            }
        }
    }
}

@Composable
private fun BudgetRow(category: String, currentLimit: Long?, onSave: (Long) -> Unit) {
    var limitText by remember { mutableStateOf(currentLimit?.toString() ?: "") }

    // Đồng bộ lại ô nhập khi DB đổi từ nơi khác (vd. vừa lưu xong) — nhưng
    // không đè chữ người dùng đang gõ dở trên CHÍNH ô này.
    LaunchedEffect(currentLimit) {
        limitText = currentLimit?.toString() ?: ""
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(category, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = limitText,
            onValueChange = { limitText = it.filter { c -> c.isDigit() }.take(12) },
            placeholder = { Text("Không giới hạn") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(140.dp)
        )
        TextButton(onClick = { onSave(limitText.toLongOrNull() ?: 0L) }) { Text("Lưu") }
    }
}
