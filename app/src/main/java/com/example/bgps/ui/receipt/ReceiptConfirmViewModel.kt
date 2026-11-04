package com.example.bgps.ui.receipt

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionSource
import com.example.bgps.data.database.TransactionType
import com.example.bgps.data.repository.TransactionRepository
import com.example.bgps.domain.ocr.ReceiptParser
import com.example.bgps.location.CurrentLocationProvider
import com.example.bgps.ocr.ReceiptTextRecognizer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptConfirmViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val textRecognizer: ReceiptTextRecognizer,
    private val currentLocationProvider: CurrentLocationProvider,
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val imageUri: Uri = Uri.parse(Uri.decode(checkNotNull(savedStateHandle.get<String>("imageUri"))))

    // true khi đang chạy ML Kit; màn hình hiện spinner trong lúc này.
    private val _scanning = MutableStateFlow(true)
    val scanning: StateFlow<Boolean> = _scanning.asStateFlow()

    // Gợi ý ban đầu từ ReceiptParser, null nếu không đọc được — màn hình luôn
    // cho người dùng tự nhập/sửa dù có gợi ý hay không.
    private val _amountGuess = MutableStateFlow<Long?>(null)
    val amountGuess: StateFlow<Long?> = _amountGuess.asStateFlow()

    private val _dateGuess = MutableStateFlow<Long?>(null)
    val dateGuess: StateFlow<Long?> = _dateGuess.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        viewModelScope.launch {
            // Lỗi đọc ảnh (file hỏng, OCR lỗi...) không chặn luồng — người
            // dùng vẫn nhập tay được, chỉ là không có gợi ý.
            val text = runCatching { textRecognizer.recognize(appContext, imageUri) }.getOrNull()
            if (text != null) {
                val parsed = ReceiptParser.parse(text)
                _amountGuess.value = parsed.amount
                _dateGuess.value = parsed.date
            }
            _scanning.value = false
        }
    }

    fun save(amount: Long, date: Long, category: String, note: String?) {
        viewModelScope.launch {
            val location = currentLocationProvider.getLastLocation()
            repository.save(
                TransactionEntity(
                    amount = amount,
                    type = TransactionType.EXPENSE,
                    category = category,
                    note = note?.takeIf { it.isNotBlank() },
                    source = TransactionSource.OCR,
                    timestamp = date,
                    latitude = location?.first,
                    longitude = location?.second
                )
            )
            _saved.value = true
        }
    }
}
