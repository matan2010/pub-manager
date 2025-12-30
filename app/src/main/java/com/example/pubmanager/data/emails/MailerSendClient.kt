package com.example.pubmanager.data.emails

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class MailerSendClient(
    private val apiKey: String,
    private val httpClient: OkHttpClient = OkHttpClient()
) {
    private val client = OkHttpClient()
    fun sendTextEmail(
        fromEmail: String,
        fromName: String,
        toEmails: List<String>,
        subject: String,
        text: String
    ) {
        val json = JSONObject().apply {
            put("from", JSONObject().apply {
                put("email", fromEmail)
                put("name", fromName)
            })

            put("to", org.json.JSONArray().apply {
                toEmails.forEach { mail ->
                    put(JSONObject().apply { put("email", mail) })
                }
            })

            put("subject", subject)
            put("text", text)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.mailersend.com/v1/email")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val code = response.code
        val bodyStr = response.body?.string()

        android.util.Log.d("MAILERSEND", "code=$code body=$bodyStr")

        if (!response.isSuccessful) {
            throw Exception("MailerSend error $code: $bodyStr")
        }
    }
}