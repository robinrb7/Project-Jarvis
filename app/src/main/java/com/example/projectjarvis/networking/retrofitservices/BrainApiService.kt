package com.example.projectjarvis.networking.retrofitservices

import com.example.projectjarvis.model.BrainChatRequest
import com.example.projectjarvis.model.BrainChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface BrainApiService {
    @POST("v2/chat")
    suspend fun classifyQuery(@Body request: BrainChatRequest): BrainChatResponse
}