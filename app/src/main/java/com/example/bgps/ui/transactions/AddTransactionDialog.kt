@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.bgps.data.database.TransactionType

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Long, type: TransactionType, category: String, merchant: String?, note: String?) -> Unit,
    onScanReceipt: () -> Unit = {}
) {
    val state = rememberTransactionFormState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm giao dịch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionFormFields(state)
                TextButton(onClick = onScanReceipt) { Text("Quét hoá đơn thay vì nhập tay") }
            }
        },
        confirmButton = {
            TextButton(
                enabled = state.canSave,
                onClick = {
                    state.amount?.let { amount ->
                        onConfirm(
                            amount,
                            state.type,
                            state.category,
                            state.merchantText.trim().takeIf { it.isNotBlank() },
                            state.noteText
                        )
                    }
                }
            ) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Huỷ") }
        }
    )
}
