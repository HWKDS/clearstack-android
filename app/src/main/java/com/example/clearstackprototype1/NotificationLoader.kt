package com.example.clearstackprototype1

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.runBlocking
import org.json.JSONArray

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
            val first = messages.first()
            if(first.summary.isNotBlank()){
                SummaryStore.summaries[sender] = first.summary
                AiInsightStore.insights[sender] =
                    AiInsight(
                        summary = first.summary,
                        priority = first.Priority,
                        tasks = jsonToList(first.tasks),
                        payments = jsonToList(first.payments),
                        meetings = jsonToList(first.meetings),
                        reminders = jsonToList(first.reminders),
                        otp = first.otp.ifBlank { null }

                    )
            }
            NotificationStore.threads.add(
                thread
            )
        }
    }
    private fun jsonToList(
        json: String
    ): List<String>{
        if(json.isBlank()) return emptyList()
        val array = JSONArray(json)
        val list = mutableListOf<String>()
        for(i in 0 until array.length()){
            list.add(array.getString(i))
        }
        return list
    }
}