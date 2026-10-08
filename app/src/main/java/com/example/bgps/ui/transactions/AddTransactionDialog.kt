@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bgps.data.database.TransactionType

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Long, type: TransactionType, category: String, note: String?) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }

    val amount = amountText.toLongOrNull()
    val canSave = amount != null && amount > 0 && category.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm giao dịch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == TransactionType.EXPENSE,
                        onClick = { type = TransactionType.EXPENSE },
                        label = { Text("Chi") }
                    )
                    FilterChip(
                        selected = type == TransactionType.INCOME,
                        onClick = { type = TransactionType.INCOME },
                        label = { Text("Thu") }
                    )
                }
                OutlinedTextField(
                    value = amountText,
                    // Chỉ cho nhập chữ số, giới hạn 12 ký tự để không tràn Long
                    onValueChange = { input -> amountText = input.filter { c -> c.isDigit() }.take(12) },
                    label = { Text("Số tiền (VND)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Danh mục (VD: Ăn uống)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú (tuỳ chọn)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = { amount?.let { onConfirm(it, type, category.trim(), note) } }
            ) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Huỷ") }
        }
    )
}
