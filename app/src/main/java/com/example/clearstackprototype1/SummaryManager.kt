package com.example.clearstackprototype1

object SummaryManager {
    fun UpdateSummary(
        thread: ConversationThread
    ){
        Thread{
            val summary =
                GeminiService.summarizeMessages(
                    thread.messages.map{
                        it.message
                    }
                )
            SummaryStore.summaries[thread.sender] = summary
        }.start()
    }
}