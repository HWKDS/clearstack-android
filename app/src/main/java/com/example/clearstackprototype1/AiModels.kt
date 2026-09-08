package com.example.clearstackprototype1

data class Payment(
    val amount: Double,
    val currency: String,
    val reason: String
)

data class Meeting(
    val title: String,
    val date: String,
    val time: String,
    val location: String
)

data class Reminder(
    val text: String,
    val dueDate: String
)