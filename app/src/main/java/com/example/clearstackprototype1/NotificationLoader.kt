package com.example.clearstackprototype1

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.runBlocking

object NotificationLoader {
    fun loadThreads(
        context: Context
    ){
        val dao = DatabaseProvider
            .getDatabase(context)
            .notificationDao()

        val notifications =
            runBlocking {
                dao.getAllNotifications()
            }

        NotificationStore.threads.clear()
        val grouped = notifications.groupBy {
            it.sender
        }
        grouped.forEach{(sender, messages) ->
            val thread =
                ConversationThread(
                    appName =
                        messages.first().appName,
                    sender = sender,
                    messages =
                        mutableStateListOf<NotificationData>()
                            .apply {
                                messages.forEach{
                                    add(
                                        NotificationData(
                                            appName = it.appName,
                                            title = it.sender,
                                            message = it.message,
                                            timeStamp = it.timestamp
                                        )
                                    )
                                }
                            },
                    lastUpdated =
                        messages.maxOf{
                            it.timestamp
                        }
                )
            NotificationStore.threads.add(
                thread
            )
        }
    }
}