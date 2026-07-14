package com.example.clearstackprototype1

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.runBlocking
import org.json.JSONArray

object SummaryManager {
    private val lastSummaryTime = mutableMapOf<String, Long>()
    fun updateSummary(
        thread: ConversationThread
    ){
        val now = System.currentTimeMillis()

        val Threadkey = "${thread.appName}:${thread.sender}"
        val lastCall = lastSummaryTime[Threadkey]?: 0

        if(now - lastCall < 30000){
            return
        }

        lastSummaryTime[Threadkey]= now
        Handler(Looper.getMainLooper()).post{
            AnalysisStateStore.states[thread.sender] = AnalysisState.ANALYZING
        }
        Thread{
            val insight =
                GeminiService.analyzeConversation(
                    sender = thread.sender,
                    messages = thread.messages.map{
                        it.message
                    }
                )
            if(insight.summary.isBlank()){
                Handler(Looper.getMainLooper()).post {
                    AnalysisStateStore.states[thread.sender] = AnalysisState.FAILED
                }
                return@Thread
            }

            Handler(Looper.getMainLooper()).post{
                SummaryStore.summaries[thread.sender] = insight.summary
                AiInsightStore.insights[thread.sender] = insight
                AnalysisStateStore.states[thread.sender] = AnalysisState.SUCCESS
            }

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