package com.example.projectjarvis.networking.retrofitservices

import com.example.projectjarvis.model.GrogRequest
import com.example.projectjarvis.model.GrogResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface QueryApiService {
    @POST("v1/chat/completions")
    suspend fun getResponse(@Body request: GrogRequest): GrogResponse
}