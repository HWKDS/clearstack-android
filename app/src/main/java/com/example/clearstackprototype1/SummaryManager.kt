package com.example.clearstackprototype1

import kotlinx.coroutines.runBlocking

object SummaryManager {
    private val lastSummaryTime = mutableMapOf<String, Long>()
    fun UpdateSummary(
        thread: ConversationThread
    ){
        val now = System.currentTimeMillis()

        val lastCall = lastSummaryTime[thread.sender]?: 0

        if(now - lastCall < 30000){
            return
        }

        lastSummaryTime[thread.sender]= now

        Thread{
            val summary =
                GeminiService.summarizeMessages(
                    sender = thread.sender,
                    messages = thread.messages.map{
                        it.message
                    }
                )
            SummaryStore.summaries[thread.sender] = summary

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