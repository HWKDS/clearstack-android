package com.example.clearstackprototype1

object PriorityManager {
    fun getPriority(
        summary: String
    ): Priority{
        val text = summary.lowercase()

        return when{
            text.contains("urgent")-> Priority.HIGH
            text.contains("awaiting")-> Priority.HIGH
            text.contains("needs")-> Priority.HIGH
            text.contains("meeting")-> Priority.MEDIUM
            text.contains("deadline")-> Priority.MEDIUM
            text.contains("tomorrow")-> Priority.MEDIUM
            else -> Priority.LOW
        }

    }
}