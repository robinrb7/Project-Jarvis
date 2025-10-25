package com.example.projectjarvis.repository

import com.example.projectjarvis.model.GrogRequest
import com.example.projectjarvis.model.Message
import com.example.projectjarvis.networking.retrofitservices.QueryApiService

class RealtimeRepository(private val api: QueryApiService) {

    suspend fun getAnswer(messages: List<Message>): String {
        val request = GrogRequest(
            model = "llama-3.3-70b-versatile",
            messages = messages,
            temperature = 0.7f,
            max_completion_tokens = 2048
        )

        val response = api.getResponse(request)
        return response.choices?.firstOrNull()?.message?.content ?: "No Response"
    }
}