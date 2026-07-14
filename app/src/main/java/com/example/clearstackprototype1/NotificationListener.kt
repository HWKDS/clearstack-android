package com.example.clearstackprototype1
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.runBlocking
class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if(sbn == null) return
        val packageName = sbn.packageName
        val appName = try{
            val applicationInfo = packageManager.getApplicationInfo(packageName,0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        }catch (e: Exception){
            packageName
        }
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: "No Title"

        val message = extras.getCharSequence("android.text")?.toString()?: "no message"
        val notificationData = NotificationData(
            appName = appName,
            title = title,
            message = message,
            timeStamp = System.currentTimeMillis()
        )
        Thread{
            val dao = DatabaseProvider
                .getDatabase(this)
                .notificationDao()
            runBlocking {
                dao.insertNotification(
                    NotificationEntity(
                        appName = appName,
                        sender = title,
                        message = message,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }.start()
        Handler(Looper.getMainLooper()).post{
            val existingThread =
                NotificationStore.threads.find{
                    it.sender == title
                }
            if(existingThread != null){
                existingThread.messages.add(notificationData)
                if(existingThread.messages.size >= 2){
                    SummaryManager.updateSummary(
                        existingThread
                    )
                }
                existingThread.lastUpdated = System.currentTimeMillis()
            }
            else{
                val newThread = ConversationThread(
                    appName = appName,
                    sender = title,
                    messages = androidx.compose.runtime.mutableStateListOf(notificationData),
                    lastUpdated = System.currentTimeMillis()
                )

                NotificationStore.threads.add(0,newThread)
                SummaryStore.summaries[title] = "waiting for more mssg..."
            }
        }
    }
}