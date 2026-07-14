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
                        payments = jsonToPayments(first.payments),
                        meetings = jsonToMeetings(first.meetings),
                        reminders = jsonToReminders(first.reminders),
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
    private fun jsonToPayments(
        json: String
    ): List<Payment> {

        if (json.isBlank()) return emptyList()

        val array = JSONArray(json)
        val list = mutableListOf<Payment>()

        for (i in 0 until array.length()) {

            val obj = array.optJSONObject(i)?: continue

            list.add(
                Payment(
                    amount = obj.optDouble("amount"),
                    currency = obj.optString("currency"),
                    reason = obj.optString("reason")
                )
            )
        }

        return list
    }
    private fun jsonToMeetings(
        json: String
    ): List<Meeting> {

        if (json.isBlank()) return emptyList()

        val array = JSONArray(json)
        val list = mutableListOf<Meeting>()

        for (i in 0 until array.length()) {

            val obj = array.optJSONObject(i) ?: continue

            list.add(
                Meeting(
                    title = obj.optString("title"),
                    date = obj.optString("date"),
                    time = obj.optString("time"),
                    location = obj.optString("location")
                )
            )
        }

        return list
    }
    private fun jsonToReminders(
        json: String
    ): List<Reminder> {

        if (json.isBlank()) return emptyList()

        val array = JSONArray(json)
        val list = mutableListOf<Reminder>()

        for (i in 0 until array.length()) {

            val obj = array.optJSONObject(i) ?: continue

            list.add(
                Reminder(
                    text = obj.optString("text"),
                    dueDate = obj.optString("dueDate")
                )
            )
        }

        return list
    }
}