package com.example.bgps.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionType
import com.example.bgps.domain.CategoryClassifier
import com.example.bgps.domain.DefaultCategories

// Gom state của form thêm/sửa giao dịch vào một chỗ để hai màn hình dùng chung.
class TransactionFormState(initial: TransactionEntity?) {
    var amountText by mutableStateOf(initial?.amount?.toString() ?: "")
    var type by mutableStateOf(initial?.type ?: TransactionType.EXPENSE)
    var merchantText by mutableStateOf(initial?.merchant ?: "")

    // Danh mục giờ là chuỗi tự do (B3 phần 2 — người dùng tự thêm/sửa/xoá
    // được), không còn enum cố định nên không cần tra cứu/chuẩn hoá gì ở
    // đây — dùng thẳng giá trị đã lưu, hoặc "Khác" cho giao dịch mới.
    var category by mutableStateOf(initial?.category ?: DefaultCategories.FALLBACK_NAME)

    var noteText by mutableStateOf(initial?.note ?: "")

    // Sửa giao dịch có sẵn thì không tự ý đè danh mục người dùng đã chọn từ trước;
    // chỉ giao dịch mới (initial == null) mới được tự gợi ý theo nơi giao dịch.
    var categoryManuallySet by mutableStateOf(initial != null)

    val amount: Long? get() = amountText.toLongOrNull()
    val canSave: Boolean get() = (amount ?: 0L) > 0L
}

@Composable
fun rememberTransactionFormState(initial: TransactionEntity? = null): TransactionFormState =
    remember(initial?.id) { TransactionFormState(initial) }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionFormFields(state: TransactionFormState) {
    LaunchedEffect(state.merchantText) {
        if (!state.categoryManuallySet) {
            CategoryClassifier.suggest(state.merchantText)?.let { state.category = it }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.type == TransactionType.EXPENSE,
                onClick = { state.type = TransactionType.EXPENSE },
                label = { Text("Chi") }
            )
            FilterChip(
                selected = state.type == TransactionType.INCOME,
                onClick = { state.type = TransactionType.INCOME },
                label = { Text("Thu") }
            )
        }
        OutlinedTextField(
            value = state.amountText,
            // Chỉ cho nhập chữ số, giới hạn 12 ký tự để không tràn Long
            onValueChange = { input -> state.amountText = input.filter { c -> c.isDigit() }.take(12) },
            label = { Text("Số tiền (VND)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        OutlinedTextField(
            value = state.merchantText,
            onValueChange = { state.merchantText = it },
            label = { Text("Nơi giao dịch (VD: Highlands, Grab)") },
            singleLine = true
        )
        Text("Danh mục")
        CategoryChipGrid(
            selected = state.category,
            onSelect = {
                state.category = it
                state.categoryManuallySet = true
            }
        )
        OutlinedTextField(
            value = state.noteText,
            onValueChange = { state.noteText = it },
            label = { Text("Ghi chú (tuỳ chọn)") },
            singleLine = true
        )
    }
}

// Tách riêng để màn xác nhận hoá đơn (ReceiptConfirmScreen) dùng lại được,
// không phải chép lại lưới chip danh mục. Tự đọc danh sách danh mục hiện có
// qua CategoryChipGridViewModel thay vì nhận qua tham số — nơi gọi chỉ cần
// quan tâm tên đang chọn, không cần biết/truyền toàn bộ danh sách.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChipGrid(
    selected: String,
    onSelect: (String) -> Unit,
    viewModel: CategoryChipGridViewModel = hiltViewModel()
) {
    val categories by viewModel.categoryNames.collectAsState()

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.forEach { name ->
            FilterChip(
                selected = selected == name,
                onClick = { onSelect(name) },
                label = { Text(name) }
            )
        }
    }
}
