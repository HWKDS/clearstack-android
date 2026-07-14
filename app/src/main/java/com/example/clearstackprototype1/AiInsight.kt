package com.example.clearstackprototype1

data class AiInsight (
    val summary: String,
    val priority: String,
    val tasks: List<String>,
    val payments: List<Payment>,
    val meetings: List<Meeting>,
    val reminders: List<Reminder>,
    val otp: String?
)