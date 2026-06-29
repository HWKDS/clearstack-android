package com.example.clearstackprototype1

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.runBlocking
import org.json.JSONArray

object SummaryManager {
    private val lastSummaryTime = mutableMapOf<String, Long>()
    fun UpdateSummary(
        thread: ConversationThread
    ){
        val now = System.currentTimeMillis()

        val Threadkey = "${thread.appName}:${thread.sender}"
        val lastCall = lastSummaryTime[Threadkey]?: 0

        if(now - lastCall < 30000){
            return
        }

        lastSummaryTime[Threadkey]= now

        Thread{
            val insight =
                GeminiService.analyzeConversation(
                    sender = thread.sender,
                    messages = thread.messages.map{
                        it.message
                    }
                )
            if(insight.summary.isBlank()){
                return@Thread
            }

            Handler(Looper.getMainLooper()).post{
                SummaryStore.summaries[thread.sender] = insight.summary
                AiInsightStore.insights[thread.sender] = insight
            }

            val priority =
                PriorityManager
                    .getPriority(insight.summary)
                    .name
            val dao =
                DatabaseProvider
                    .getDatabase(
                        AppContextHolder.context
                    ).notificationDao()
            runBlocking {
                dao.updateSummary(
                    sender = thread.sender,
                    summary = insight.summary,
                    priority = insight.priority,
                    tasks = JSONArray(insight.tasks).toString(),
                    payments = JSONArray(insight.payments).toString(),
                    meetings = JSONArray(insight.meetings).toString(),
                    reminders = JSONArray(insight.reminders).toString(),
                    otp = insight.otp?: ""
                    )
            }
        }.start()
    }
}