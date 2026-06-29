package com.example.clearstackprototype1
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotificationDao {

    @Insert
    suspend fun insertNotification(
        notification: NotificationEntity
    )

    @Query(
        "SELECT * FROM notifications ORDER BY timestamp DESC"
    )suspend fun getAllNotifications():
            List<NotificationEntity>

    @Query(
        "DELETE FROM notifications"
    )suspend fun clearAll()
    @Query(
        "DELETE FROM notifications WHERE sender = :sender"
    )suspend fun deleteThread(
        sender: String
    )

    @Query(
        """
    UPDATE notifications
    SET summary = :summary,
        priority = :priority,
        tasks = :tasks,
        payments = :payments,
        meetings = :meetings,
        reminders = :reminders,
        otp = :otp
    WHERE sender = :sender
    """
    )suspend fun updateSummary(
        sender: String,
        summary: String,
        priority: String,
        tasks: String,
        payments: String,
        meetings: String,
        reminders: String,
        otp: String
    )
}