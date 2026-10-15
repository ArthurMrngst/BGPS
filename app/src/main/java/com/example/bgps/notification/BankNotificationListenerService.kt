package com.example.bgps.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.bgps.data.database.TransactionEntity
import com.example.bgps.data.database.TransactionSource
import com.example.bgps.data.repository.TransactionRepository
import com.example.bgps.domain.Category
import com.example.bgps.domain.CategoryClassifier
import com.example.bgps.domain.parsing.NotificationParsers
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

        scope.launch {
            repository.save(
                TransactionEntity(
                    amount = parsed.amount,
                    type = parsed.type,
                    category = (CategoryClassifier.suggest(parsed.merchant) ?: Category.default).displayName,
                    merchant = parsed.merchant,
                    source = TransactionSource.NOTIFICATION,
                    timestamp = parsed.timestamp ?: sbn.postTime
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
