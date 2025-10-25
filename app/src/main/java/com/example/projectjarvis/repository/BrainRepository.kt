package com.example.projectjarvis.repository

import android.util.Log
import com.example.projectjarvis.model.BrainChatRequest
import com.example.projectjarvis.model.Message
import com.example.projectjarvis.networking.retrofitservices.BrainApiService
import com.google.gson.Gson
import retrofit2.HttpException

class BrainRepository(private val apiService: BrainApiService) {

    suspend fun classifyQuery(
        preamble: String,
        chatHistory: List<Message>,
        userQuery: String
    ): String?{


        return try {
            val messages = mutableListOf<Message>()

            messages.add(Message("system", preamble))
            messages.addAll(chatHistory)
            messages.add(Message("user", userQuery))

            val request = BrainChatRequest(
                model = "command-a-03-2025",
                messages = messages
            )

            val response = apiService.classifyQuery(request)
            Log.i("BrainRepo", "Response: $response")
            Log.i("BrainRaw", Gson().toJson(response))
            response.message.content.firstOrNull()?.text ?: "No response"

        } catch (e: retrofit2.HttpException) {
            Log.e("BrainRepo", "HTTP Error: ${e.code()} - ${e.response()?.errorBody()?.string()}", e)
            null
        } catch (e: Exception) {
            Log.e("BrainRepo", "Unexpected Error: ${e.message}", e)
            null
        }

    }
}