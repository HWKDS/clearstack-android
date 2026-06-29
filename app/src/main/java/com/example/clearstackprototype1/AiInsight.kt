package com.example.clearstackprototype1

data class AiInsight (
    val summary: String,
    val priority: String,
    val tasks: List<String>,
    val payments: List<String>,
    val meetings: List<String>,
    val reminders: List<String>,
    val otp: String?
)