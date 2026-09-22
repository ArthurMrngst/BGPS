package com.example.bgps.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class BankNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        // TODO: whitelist-filter by sbn.packageName before parsing/storing anything.
    }
}
