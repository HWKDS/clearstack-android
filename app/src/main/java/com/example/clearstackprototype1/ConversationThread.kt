package com.example.clearstackprototype1

import androidx.compose.runtime.snapshots.SnapshotStateList

data class ConversationThread (
    val sender: String,
    val messages: SnapshotStateList<NotificationData>,
    var lastUpdated: Long
)