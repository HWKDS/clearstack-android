package com.example.clearstackprototype1

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.runBlocking
import org.json.JSONArray

object SummaryManager {
    private val runningThreads = mutableSetOf<String>()
    fun updateSummary(
        thread: ConversationThread
    ){
        val threadKey = "${thread.appName}:${thread.sender}"

        if(threadKey in runningThreads){
            return
        }
        runningThreads.add(threadKey)

        Handler(Looper.getMainLooper()).post{
            AnalysisStateStore.states[thread.sender] = AnalysisState.ANALYZING
        }
        Thread{
            try{
                val insight =
                    GeminiService.analyzeConversation(
                        sender = thread.sender,
                        messages = thread.messages.map{
                            it.message
                        }
                    )
                if(insight.summary.isBlank()){
                    Handler(Looper.getMainLooper()).post {
                        if(AiInsightStore.insights.contains(thread.sender)){
                            //we already have old analysis
                            //keep showing it
                            RefreshStateStore.refreshFailed[thread.sender] = true
                            AnalysisStateStore.states[thread.sender] = AnalysisState.SUCCESS
                        }else{
                            //first ever analysis failed.
                            AnalysisStateStore.states[thread.sender] = AnalysisState.FAILED
                        }
                    }
                    return@Thread
                }

                Handler(Looper.getMainLooper()).post{
                    SummaryStore.summaries[thread.sender] = insight.summary
                    AiInsightStore.insights[thread.sender] = insight
                    RefreshStateStore.refreshFailed[thread.sender] = false
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
            }
            finally {
                runningThreads.remove(threadKey)
            }
        }.start()
    }
}