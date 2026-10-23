@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bgps.ui.receipt

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bgps.domain.Category
import com.example.bgps.ui.transactions.CategoryChipGrid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptConfirmScreen(
    onDone: () -> Unit,
    viewModel: ReceiptConfirmViewModel = hiltViewModel()
) {
    val scanning by viewModel.scanning.collectAsState()
    val amountGuess by viewModel.amountGuess.collectAsState()
    val dateGuess by viewModel.dateGuess.collectAsState()
    val saved by viewModel.saved.collectAsState()

    var amountText by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var category by remember { mutableStateOf(Category.default) }
    var noteText by remember { mutableStateOf("") }
    var prefilled by remember { mutableStateOf(false) }

    // Chỉ điền sẵn một lần khi OCR xong, để không ghi đè giá trị người dùng
    // đã tự sửa tay nếu màn hình recompose lại.
    LaunchedEffect(scanning) {
        if (!scanning && !prefilled) {
            amountGuess?.let { amountText = it.toString() }
            dateGuess?.let { dateMillis = it }
            prefilled = true
        }
    }

    LaunchedEffect(saved) {
        if (saved) onDone()
    }

    val context = LocalContext.current
    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(viewModel.imageUri) {
        thumbnail = withContext(Dispatchers.IO) { decodeSampledBitmap(context, viewModel.imageUri, 900) }
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xác nhận hoá đơn") },
                navigationIcon = { TextButton(onClick = onDone) { Text("Quay lại") } }
            )
        }
    ) { innerPadding ->
        if (scanning) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("Đang đọc hoá đơn...")
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                thumbnail?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Ảnh hoá đơn",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                }
                if (amountGuess == null) {
                    Text(
                        "Không tự đọc được số tiền từ hoá đơn, nhập tay nhé.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input -> amountText = input.filter { it.isDigit() }.take(12) },
                    label = { Text("Số tiền (VND)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Column {
                    Text("Ngày", style = MaterialTheme.typography.labelMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(dateFormatter.format(Date(dateMillis)), style = MaterialTheme.typography.bodyLarge)
                        TextButton(onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val picked = Calendar.getInstance().apply {
                                        set(year, month, day, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    dateMillis = picked.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) { Text("Đổi ngày") }
                    }
                }
                Text("Danh mục")
                CategoryChipGrid(selected = category, onSelect = { category = it })
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Ghi chú (tuỳ chọn)") },
                    singleLine = true
                )
                val amount = amountText.toLongOrNull()
                TextButton(
                    enabled = amount != null && amount > 0,
                    onClick = {
                        amount?.let { viewModel.save(it, dateMillis, category.displayName, noteText) }
                    }
                ) { Text("Lưu") }
            }
        }
    }
}

// Giải mã ảnh với kích thước thu nhỏ để tránh OutOfMemory với ảnh chụp độ
// phân giải cao — chỉ dùng để hiển thị xem lại, không ảnh hưởng đến OCR
// (ML Kit đọc trực tiếp từ Uri gốc, không qua bitmap này).
private fun decodeSampledBitmap(
    context: android.content.Context,
    uri: android.net.Uri,
    maxDimensionPx: Int
): Bitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > maxDimensionPx || bounds.outHeight / sampleSize > maxDimensionPx) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
} catch (e: Exception) {
    null
}
