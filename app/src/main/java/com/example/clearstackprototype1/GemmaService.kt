package com.example.clearstackprototype1

import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

object GemmaService {

    private var engine: Engine? = null

    /**
     * Call once, off the main thread, before the first analyzeConversation() call.
     * e.g. from AppContextHolder's init, or Application.onCreate().
     */
    fun initialize(modelPath: String) {
        if (engine != null) return
        engine = Engine(EngineConfig(modelPath = modelPath)).apply { initialize() }
    }

    fun analyzeConversation(
        sender: String,
        messages: List<String>
    ): AiInsight {
        val currentEngine = engine ?: return emptyInsight()

        return try {
            val prompt = buildPrompt(sender, messages)

            val fullText = StringBuilder()
            runBlocking {
                currentEngine.createConversation().use { conversation ->
                    conversation.sendMessageAsync(prompt).collect { chunk ->
                        fullText.append(chunk)
                    }
                }
            }

            val cleanedJson = fullText.toString()
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val aiJson = JSONObject(cleanedJson)

            AiInsight(
                summary = aiJson.getString("summary"),
                priority = aiJson.getString("priority"),
                tasks = jsonArrayToList(aiJson.getJSONArray("tasks")),
                payments = jsonArrayToPayments(aiJson.getJSONArray("payments")),
                meetings = jsonArrayToMeetings(aiJson.getJSONArray("meetings")),
                reminders = jsonArrayToReminders(aiJson.getJSONArray("reminders")),
                otp = if (aiJson.isNull("otp")) null else aiJson.getString("otp")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emptyInsight()
        }
    }

    private fun emptyInsight() = AiInsight(
        summary = "",
        priority = "LOW",
        tasks = emptyList(),
        payments = emptyList(),
        meetings = emptyList(),
        reminders = emptyList(),
        otp = null
    )

    private fun buildPrompt(sender: String, messages: List<String>): String {
        // Same schema/rules as GeminiService.analyzeConversation — copied verbatim
        return """
            You are ClearStack AI.
            Analyze the following notification conversation.
            Return ONLY valid JSON.
            Do not include markdown.
            Do not include explanations.
            Do not wrap the JSON in ```.
            Use EXACTLY this schema:
            {
            "summary": "One short sentence",
            "priority": "HIGH",
            "tasks": ["Task 1"],
            "payments": [{"amount": 0, "currency": "INR", "reason": ""}],
            "meetings": [{"title": "", "date": "", "time": "", "location": ""}],
            "reminders": [{"text": "", "dueDate": ""}],
            "otp": null
            }
            Rules:
            1. Return VALID JSON ONLY.
            2. Every field must exist.
            3. If there are no tasks, return "tasks": []
            4. If there are no payments, return "payments": []
            5. Every payment MUST be an object with amount (number), currency, reason. Never a string.
            6. Every meeting MUST be an object with title, date, time, location. Never a string.
            7. Every reminder MUST be an object with text, dueDate. Never a string.
            8. OTP should contain only the numeric code, or null if none exists.
            9. Priority can ONLY be: HIGH, MEDIUM, LOW
            10. Summary must be one short sentence.
            Sender:
            $sender

            Messages:
            ${messages.joinToString("\n")}
        """.trimIndent()
    }

    // Copy these three helpers + jsonArrayToList verbatim from GeminiService — identical logic
    private fun jsonArrayToList(array: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) list.add(array.optString(i))
        return list
    }

    private fun jsonArrayToPayments(array: JSONArray): List<Payment> {
        val list = mutableListOf<Payment>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val amount = obj.getDouble("amount")
            val currency = obj.getString("currency")
            val reason = obj.getString("reason")
            list.add(Payment(amount, currency, reason))
        }
        return list
    }

    private fun jsonArrayToMeetings(array: JSONArray): List<Meeting> {
        val list = mutableListOf<Meeting>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val title = obj.getString("title")
            val date = obj.getString("date")
            val time = obj.getString("time")
            val location = obj.getString("location")
            list.add(Meeting(title, date, time, location))
        }
        return list
    }

    private fun jsonArrayToReminders(array: JSONArray): List<Reminder> {
        val list = mutableListOf<Reminder>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val text = obj.getString("text")
            val dueDate = obj.getString("dueDate")
            list.add(Reminder(text, dueDate))
        }
        return list
    }
}