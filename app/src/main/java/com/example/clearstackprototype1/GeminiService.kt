package com.example.clearstackprototype1

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

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
                "payments": [
                {
                      "amount": 0,
                      "currency": "INR",
                      "reason": ""
                      }
                ],
                "meetings": [
                    {
                          "title": "",
                          "date": "",
                          "time": "",
                          "location": ""
                    }
                ],
                "reminders": [
                    {
                          "text": "",
                          "dueDate": ""
                    }
                ],
                "otp": null
                }
                Rules:
1. Return VALID JSON ONLY.
2. Every field must exist.
3. If there are no tasks, return:
   "tasks": []
4. If there are no payments:
   "payments": []
5. Every payment MUST be an object:
{
  "amount": number,
  "currency": "INR",
  "reason": "string"
}
Never return payment strings.
6. Every meeting MUST be an object:
{
  "title": "...",
  "date": "...",
  "time": "...",
  "location": "..."
}
Never return meeting strings.
7. Every reminder MUST be an object:
{
  "text": "...",
  "dueDate": "..."
}
Never return reminder strings.
8. OTP should contain only the numeric code.
If none exists use null.
9. Priority can ONLY be:
HIGH
MEDIUM
LOW
10. Summary must be one short sentence.
Sender:
$sender

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
                payments = jsonArrayToPayments(aiJson.getJSONArray("payments")),
                meetings = jsonArrayToMeetings(aiJson.getJSONArray("meetings")),
                reminders = jsonArrayToReminders(aiJson.getJSONArray("reminders")),
                otp = if (aiJson.isNull("otp")) null else aiJson.getString("otp")
            )

        } catch (e: Exception) {

            e.printStackTrace()

            AiInsight(
                summary = "",
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
            list.add(array.optString(i))
        }
        return list
    }
    private fun jsonArrayToPayments(
        array: JSONArray
    ): List<Payment> {
        val list = mutableListOf<Payment>()

        for (i in 0 until array.length()) {

            val item = array.opt(i)

            if (item is JSONObject) {

                list.add(
                    Payment(
                        amount = item.optDouble("amount"),
                        currency = item.optString("currency", "INR"),
                        reason = item.optString("reason", "")
                    )
                )

            } else {

                list.add(
                    Payment(
                        amount = 0.0,
                        currency = "INR",
                        reason = item.toString()
                    )
                )

            }
        }
        return list
    }
    private fun jsonArrayToMeetings(
        array: JSONArray
    ): List<Meeting> {

        val list = mutableListOf<Meeting>()

        for (i in 0 until array.length()) {

            val item = array.opt(i)

            if (item is JSONObject) {

                list.add(
                    Meeting(
                        title = item.optString("title"),
                        date = item.optString("date"),
                        time = item.optString("time"),
                        location = item.optString("location")
                    )
                )

            } else {

                list.add(
                    Meeting(
                        title = item.toString(),
                        date = null,
                        time = null,
                        location = null
                    )
                )

            }
        }

        return list
    }
    private fun jsonArrayToReminders(
        array: JSONArray
    ): List<Reminder> {

        val list = mutableListOf<Reminder>()

        for (i in 0 until array.length()) {

            val item = array.opt(i)

            if (item is JSONObject) {

                list.add(
                    Reminder(
                        text = item.optString("text"),
                        dueDate = item.optString("dueDate")
                    )
                )

            } else {

                list.add(
                    Reminder(
                        text = item.toString(),
                        dueDate = null
                    )
                )

            }
        }

        return list
    }
}