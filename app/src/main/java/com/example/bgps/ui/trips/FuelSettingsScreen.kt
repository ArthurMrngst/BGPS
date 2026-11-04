@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

// Cho người dùng tự chỉnh mức tiêu hao xăng (L/100km) theo xe thật của họ và
// giá xăng hiện tại, thay vì chỉ dùng số mặc định tham khảo trong
// FuelCostCalculator — số càng sát xe/giá thật thì tiền xăng ước tính ở các
// màn hành trình càng chính xác.
@Composable
fun FuelSettingsScreen(
    onBack: () -> Unit,
    viewModel: FuelSettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    var motorbikeText by remember { mutableStateOf("") }
    var carText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        if (!initialized) {
            motorbikeText = settings.motorbikeLitersPer100Km.toString()
            carText = settings.carLitersPer100Km.toString()
            priceText = settings.pricePerLiterVnd.toString()
            initialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt xăng") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Quay lại") } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Số càng sát xe thật của bạn thì tiền xăng ước tính càng chính xác.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = motorbikeText,
                onValueChange = { motorbikeText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Xe máy (L/100km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = carText,
                onValueChange = { carText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Ô tô (L/100km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Giá xăng (đ/L)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val motorbike = motorbikeText.toDoubleOrNull() ?: settings.motorbikeLitersPer100Km
                    val car = carText.toDoubleOrNull() ?: settings.carLitersPer100Km
                    val price = priceText.toDoubleOrNull() ?: settings.pricePerLiterVnd
                    viewModel.save(motorbike, car, price)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lưu")
            }
        }
    }
}
