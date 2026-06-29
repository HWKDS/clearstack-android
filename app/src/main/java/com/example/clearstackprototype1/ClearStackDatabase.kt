package com.example.clearstackprototype1

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NotificationEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ClearStackDatabase : RoomDatabase(){
    abstract fun notificationDao(): NotificationDao
}