package com.example.projectjarvis.repository

import com.example.projectjarvis.model.GrogRequest
import com.example.projectjarvis.model.Message
import com.example.projectjarvis.networking.retrofitservices.QueryApiService

class GeneralRepository(private val api: QueryApiService) {

    suspend fun askQuestion(
        systemPrompt: String,
        realtimeInfo: String,
        userQuery: String
    ): String {
        val messages = mutableListOf<Message>()
        messages.add(Message(role = "system", content = "$systemPrompt\n$realtimeInfo"))
        messages.add(Message(role = "user", content = userQuery))

        val request = GrogRequest(
            model = "llama-3.3-70b-versatile",
            messages = messages,
            temperature = 0.7f,
            max_completion_tokens =  1024
        )

        return try {
            val response = api.getResponse(request)
            val output = response.choices?.firstOrNull()?.message?.content
            android.util.Log.i("GeneralRepo", "Groq Raw: $response")
            output ?: "No response from model"
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("GeneralRepo", "HTTP ${e.code()}: $errorBody")
            "HTTP Error: ${e.code()}"
        } catch (e: Exception) {
            android.util.Log.e("GeneralRepo", "Error: ${e.message}", e)
            "Error processing query."
        }
    }
}