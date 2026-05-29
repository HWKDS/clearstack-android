package com.example.clearstackprototype1

import androidx.compose.runtime.snapshots.SnapshotStateList
data class ConversationThread (
    val appName: String,
    val sender: String,
    val messages: SnapshotStateList<NotificationData>,
    var lastUpdated : Long
){}