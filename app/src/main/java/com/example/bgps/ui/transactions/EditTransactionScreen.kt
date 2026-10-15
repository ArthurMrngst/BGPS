@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun EditTransactionScreen(
    onDone: () -> Unit,
    viewModel: EditTransactionViewModel = hiltViewModel()
) {
    val transaction by viewModel.transaction.collectAsState()
    val loaded by viewModel.loaded.collectAsState()
    val done by viewModel.done.collectAsState()

    // Sửa hoặc xoá xong thì quay lại danh sách; danh sách tự cập nhật qua Flow của Room.
    LaunchedEffect(done) {
        if (done) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sửa giao dịch") },
                navigationIcon = { TextButton(onClick = onDone) { Text("Quay lại") } }
            )
        }
    ) { innerPadding ->
        val currentTransaction = transaction
        when {
            !loaded -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Đang tải...") }

            currentTransaction == null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Giao dịch không còn tồn tại") }

            else -> {
                val state = rememberTransactionFormState(currentTransaction)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TransactionFormFields(state)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            enabled = state.canSave,
                            onClick = {
                                state.amount?.let { amount ->
                                    viewModel.save(
                                        amount = amount,
                                        type = state.type,
                                        category = state.category.displayName,
                                        merchant = state.merchantText.trim().takeIf { it.isNotBlank() },
                                        note = state.noteText
                                    )
                                }
                            }
                        ) { Text("Lưu") }
                        TextButton(onClick = { viewModel.delete() }) { Text("Xoá giao dịch") }
                    }
                }
            }
        }
    }
}
