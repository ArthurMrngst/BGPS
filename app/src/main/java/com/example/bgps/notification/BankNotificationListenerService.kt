package com.example.bgps.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionSource
import com.example.bgps.data.repository.TransactionRepository
import com.example.bgps.domain.CategoryClassifier
import com.example.bgps.domain.DefaultCategories
import com.example.bgps.domain.parsing.NotificationParsers
import com.example.bgps.location.CurrentLocationProvider
import com.example.bgps.location.LastKnownLocationStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BankNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var repository: TransactionRepository

    @Inject
    lateinit var lastKnownLocationStore: LastKnownLocationStore

    @Inject
    lateinit var currentLocationProvider: CurrentLocationProvider

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        // Whitelist theo package: chỉ đọc nội dung của app ngân hàng/ví đã có
        // parser đăng ký trong NotificationParsers; bỏ qua mọi app khác trước
        // khi chạm vào extras của notification.
        val parser = NotificationParsers.forPackage(sbn.packageName) ?: return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString() ?: ""
        if (text.isBlank()) return

        // Nội dung thô (title/text) chỉ sống trong hàm này để đưa vào parser;
        // không log, không lưu nguyên văn ở bất kỳ đâu (đúng ràng buộc riêng tư).
        val parsed = parser.parse(title, text) ?: return

        val timestamp = parsed.timestamp ?: sbn.postTime

        scope.launch {
            // Chặn lưu trùng khi hệ thống đăng/cập nhật lại cùng một thông báo
            // (xảy ra thật trên một số máy) — xem TransactionRepository.
            // findDuplicateNotificationTransaction.
            val duplicate = repository.findDuplicateNotificationTransaction(
                transactionRef = parsed.transactionRef,
                amount = parsed.amount,
                type = parsed.type,
                timestamp = timestamp
            )
            if (duplicate != null) return@launch

            // Ưu tiên điểm ghi được từ A6 (hành trình đang ghi, nếu có) vì nó
            // chính xác và mới hơn; không có thì mới xin lastLocation của hệ
            // thống (cần ACCESS_BACKGROUND_LOCATION vì service này chạy nền).
            // Không có cả hai thì để null, giao dịch vẫn lưu bình thường.
            val location = lastKnownLocationStore.getRecent()
                ?: currentLocationProvider.getLastLocation(requireBackgroundPermission = true)

            repository.save(
                TransactionEntity(
                    amount = parsed.amount,
                    type = parsed.type,
                    category = CategoryClassifier.suggest(parsed.merchant) ?: DefaultCategories.FALLBACK_NAME,
                    merchant = parsed.merchant,
                    source = TransactionSource.NOTIFICATION,
                    timestamp = timestamp,
                    latitude = location?.first,
                    longitude = location?.second,
                    transactionRef = parsed.transactionRef
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
