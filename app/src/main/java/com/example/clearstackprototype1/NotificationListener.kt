package com.example.clearstackprototype1
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if(sbn == null) return
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: "No Title"

        val message = extras.getCharSequence("android.text")?.toString()?: "no message"
        val notificationData = NotificationData(
            appName = packageName,
            title = title,
            message = message,
            timeStamp = System.currentTimeMillis()
        )
        NotificationStore.notifications.add(0,notificationData)
        Log.d(
            "ClearStack",
            """
            -------------------------
            App: $packageName
            Title: $title
            Message: $message
            -------------------------
            """.trimIndent()
        )
    }
}