package com.example.bgps.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Bọc ML Kit Text Recognition (chạy ngay trên máy, ảnh không gửi lên server
// nào) thành một hàm suspend để gọi từ ViewModel. Giữ một client dùng lại
// cho cả vòng đời app thay vì tạo/đóng mỗi lần quét.
@Singleton
class ReceiptTextRecognizer @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognize(context: Context, imageUri: Uri): String =
        suspendCancellableCoroutine { continuation ->
            val image = try {
                InputImage.fromFilePath(context, imageUri)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
                return@suspendCancellableCoroutine
            }
            recognizer.process(image)
                .addOnSuccessListener { visionText -> continuation.resume(visionText.text) }
                .addOnFailureListener { e -> continuation.resumeWithException(e) }
        }
}
