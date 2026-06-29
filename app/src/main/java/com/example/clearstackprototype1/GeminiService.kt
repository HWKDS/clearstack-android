package com.example.clearstackprototype1

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

object GeminiService {
    private const val API_KEY = "AQ.Ab8RN6I_eSdY2crfMKG3NUolv8FM4rOmUPrEMxsgFpDernalxg"
    private val client = OkHttpClient()

    fun summarizeMessages(
        sender : String,
        messages: List<String>
    ): String{
        return try{
            val prompt = """
                You are ClearStack, a personal notification assistant.
                Your job is to read a conversation thread and tell the user what matters.
                Rules:
                - Write exactly one short sentence.
                - Speak like a personal assistant.
                - Mention urgency if present.
                - Mention requests, money, meetings, deadlines or important actions.
                - Ignore greetings, emojis and filler words.
                - Do not repeat the messages verbatim.
                - Do not use quotation marks.
                Sender: ${sender}
                Messages:
                ${messages.joinToString("\n")}
                """.trimIndent()
            val requestJson = JSONObject()

            val part = JSONObject()
                .put("text", prompt)

            val parts = JSONArray()
                .put(part)

            val content = JSONObject()
                .put("parts", parts)

            val contents = JSONArray()
                .put(content)


            requestJson.put(
                "contents",
                contents
            )
            val body = requestJson
                .toString()
                .toRequestBody(
                    "application/json".toMediaType()
                )
            val request = Request.Builder()
                .url(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=$API_KEY"
                ).post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody =
                response.body?.string()
                    ?: return "NO response"

            val json = JSONObject(responseBody)
            json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        }catch (e: Exception){
            e.printStackTrace()
            "Summary unavailable"
        }
    }
    fun analyzeConversation(
        sender: String,
        messages: List<String>
    ): AiInsight {
        return try {

            val prompt = """
            You are ClearStack AI.

            Analyze this notification conversation.

            Return ONLY valid JSON.

            Schema:

            {
              "summary": "",
              "priority": "HIGH | MEDIUM | LOW",
              "tasks": [],
              "payments": [],
              "meetings": [],
              "reminders": [],
              "otp": null
            }

            Rules:

            - summary must be one short sentence.
            - tasks should contain actions the user should perform.
            - payments should contain money requests.
            - meetings should contain meeting details.
            - reminders should contain dates or deadlines.
            - otp must contain the OTP code if present, otherwise null.
            - priority must be HIGH, MEDIUM or LOW.
            - Return JSON only.

            Sender: $sender

            Messages:
            ${messages.joinToString("\n")}
        """.trimIndent()

            val requestJson = JSONObject()

            val part = JSONObject()
                .put("text", prompt)

            val parts = JSONArray()
                .put(part)

            val content = JSONObject()
                .put("parts", parts)

            val contents = JSONArray()
                .put(content)

            requestJson.put(
                "contents",
                contents
            )

            val body = requestJson
                .toString()
                .toRequestBody(
                    "application/json".toMediaType()
                )

            val request = Request.Builder()
                .url(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=$API_KEY"
                )
                .post(body)
                .build()

            val response = client.newCall(request).execute()

            val responseBody =
                response.body?.string()
                    ?: throw Exception("No response")

            println(responseBody)

            val json = JSONObject(responseBody)

            val text = json
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            val cleanedJson = text
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val aiJson = JSONObject(cleanedJson)

            AiInsight(
                summary = aiJson.getString("summary"),
                priority = aiJson.getString("priority"),
                tasks = jsonArrayToList(aiJson.getJSONArray("tasks")),
                payments = jsonArrayToList(aiJson.getJSONArray("payments")),
                meetings = jsonArrayToList(aiJson.getJSONArray("meetings")),
                reminders = jsonArrayToList(aiJson.getJSONArray("reminders")),
                otp = if (aiJson.isNull("otp")) null else aiJson.getString("otp")
            )

        } catch (e: Exception) {

            e.printStackTrace()

            AiInsight(
                summary = "Analysis unavailable",
                priority = "LOW",
                tasks = emptyList(),
                payments = emptyList(),
                meetings = emptyList(),
                reminders = emptyList(),
                otp = null
            )
        }
    }
    private fun jsonArrayToList(
        array: JSONArray
    ): List<String> {
        val list = mutableListOf<String>()
        for(i in 0 until array.length()){
            list.add(array.getString(i))
        }
        return list
    }
}