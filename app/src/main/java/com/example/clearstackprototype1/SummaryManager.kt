package com.example.clearstackprototype1

object SummaryManager {
    fun UpdateSummary(
        thread: ConversationThread
    ){
        Thread{
            val summary =
                GeminiService.summarizeMessages(
                    sender = thread.sender,
                    messages = thread.messages.map{
                        it.message
                    }
                )
            SummaryStore.summaries[thread.sender] = summary
        }.start()
    }
}