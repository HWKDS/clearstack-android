package com.example.clearstackprototype1

object TimeUtils {
    fun getTimeAgo(
        timeStamp: Long
    ): String{
        val diff = System.currentTimeMillis() - timeStamp
        val minutes = diff / 60000

        return when{
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            else -> "${minutes / 60}h ago"
        }
    }
}