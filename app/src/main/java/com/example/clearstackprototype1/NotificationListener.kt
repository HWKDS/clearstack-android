package com.example.clearstackprototype1
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.os.Handler
import android.os.Looper
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
        Handler(Looper.getMainLooper()).post{
            val existingThread =
                NotificationStore.threads.find{
                    it.sender == title
                }
            if(existingThread != null){
                existingThread.messages.add(notificationData)
                existingThread.lastUpdated = System.currentTimeMillis()
            }
            else{
                val newThread = ConversationThread(
                    sender = title,
                    messages = androidx.compose.runtime.mutableStateListOf(notificationData),
                    lastUpdated = System.currentTimeMillis()
                )

                NotificationStore.threads.add(0,newThread)
            }
        }
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