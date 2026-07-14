package com.example.clearstackprototype1
import androidx.compose.runtime.mutableStateMapOf

object RefreshStateStore {
    val refreshFailed = mutableStateMapOf<String, Boolean>()
}