@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

// Gộp đăng nhập/đăng ký/đăng xuất/đồng bộ vào một màn — đăng nhập hoàn toàn
// TUỲ CHỌN, dùng để bật đồng bộ lên backend; không đăng nhập vẫn dùng app
// bình thường với dữ liệu cục bộ trên máy (chế độ khách, mặc định của app).
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val loggedInEmail by viewModel.loggedInEmail.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tài khoản") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Quay lại") } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            if (loggedInEmail != null) {
                LoggedInContent(email = loggedInEmail!!, uiState = uiState, viewModel = viewModel)
            } else {
                LoginForm(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun LoggedInContent(email: String, uiState: AccountUiState, viewModel: AccountViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Đã đăng nhập: $email", style = MaterialTheme.typography.titleMedium)
        Text(
            "Đồng bộ đẩy các giao dịch mới/sửa trên máy này lên server, và tải về " +
                "thay đổi từ máy khác (nếu có). Chỉ đồng bộ giao dịch — hành trình, " +
                "ngân sách, danh mục vẫn đang lưu riêng trên từng máy.",
            style = MaterialTheme.typography.bodyMedium
        )
        when (uiState) {
            is AccountUiState.Loading -> CircularProgressIndicator()
            is AccountUiState.Error -> Text(uiState.message, color = MaterialTheme.colorScheme.error)
            is AccountUiState.SyncSuccess -> Text("Đồng bộ xong.")
            AccountUiState.Idle -> {}
        }
        Button(onClick = viewModel::syncNow, modifier = Modifier.fillMaxWidth()) {
            Text("Đồng bộ ngay")
        }
        TextButton(onClick = viewModel::logout) { Text("Đăng xuất") }
    }
}

@Composable
private fun LoginForm(uiState: AccountUiState, viewModel: AccountViewModel) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(isRegisterMode) { viewModel.clearError() }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Đăng nhập để bật đồng bộ giao dịch lên server — không đăng nhập " +
                "vẫn dùng được app bình thường với dữ liệu trên máy.",
            style = MaterialTheme.typography.bodyMedium
        )
        Row {
            FilterChip(
                selected = !isRegisterMode,
                onClick = { isRegisterMode = false },
                label = { Text("Đăng nhập") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = isRegisterMode,
                onClick = { isRegisterMode = true },
                label = { Text("Đăng ký") }
            )
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        when (uiState) {
            is AccountUiState.Loading -> CircularProgressIndicator()
            is AccountUiState.Error -> Text(uiState.message, color = MaterialTheme.colorScheme.error)
            else -> {}
        }
        Button(
            enabled = email.isNotBlank() && password.isNotBlank() && uiState !is AccountUiState.Loading,
            onClick = {
                if (isRegisterMode) viewModel.register(email.trim(), password) else viewModel.login(email.trim(), password)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRegisterMode) "Đăng ký" else "Đăng nhập")
        }
    }
}
