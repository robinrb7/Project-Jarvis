package com.example.projectjarvis.networking.retrofitservices

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TTSApiService {
    @GET("tts")
    suspend fun getSpeech(@Query("text") text: String): Response<ResponseBody>
}