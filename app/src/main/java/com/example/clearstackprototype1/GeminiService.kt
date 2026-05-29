package com.example.clearstackprototype1

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
object GeminiService {
    private const val API_KEY = "your api key"
    private val client = OkHttpClient()

    fun summarizeMessages(
        messages: List<String>
    ): String{
        return try{
            val prompt = """
                You are a notification summarizer.
                
                Summarize these messages in ONE SHORT sentence.
                Messages:
                ${messages.joinToString("\n") }
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
}