package com.example.clearstackprototype1

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.runBlocking

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
            val summary =
                GeminiService.summarizeMessages(
                    sender = thread.sender,
                    messages = thread.messages.map{
                        it.message
                    }
                )
            if(summary.isBlank()){
                return@Thread
            }

            Handler(Looper.getMainLooper()).post{
                SummaryStore.summaries[thread.sender] = summary
            }

            val priority =
                PriorityManager
                    .getPriority(summary)
                    .name
            val dao =
                DatabaseProvider
                    .getDatabase(
                        AppContextHolder.context
                    ).notificationDao()
            runBlocking {
                dao.updateSummary(
                    sender = thread.sender,
                    summary = summary,
                    priority = priority
                )
            }
        }.start()
    }
}