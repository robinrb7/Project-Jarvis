package com.example.projectjarvis.networking

import com.example.projectjarvis.networking.retrofitservices.BrainApiService
import com.example.projectjarvis.networking.retrofitservices.QueryApiService
import com.example.projectjarvis.networking.retrofitservices.SpotifyApiService
import com.example.projectjarvis.networking.retrofitservices.TTSApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import kotlin.getValue

object RetrofitClient {

    // -----------------------------
    // TTS API (Edge TTS backend)
    // -----------------------------
    val ttsApiService: TTSApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://web-production-1a4b4.up.railway.app/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(TTSApiService::class.java)
    }

    // -----------------------------
    // Brain LLM API
    // -----------------------------
    fun brainApiService(apiKey: String): BrainApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()
                chain.proceed(request)
            }.build()

        return Retrofit.Builder()
            .baseUrl("https://api.cohere.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BrainApiService::class.java)
    }

    // -----------------------------
    // General / Realtime / Automation API (Groq)
    // -----------------------------
    fun queryApiService(apiKey: String): QueryApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()
                chain.proceed(request)
            }.build()

        return Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QueryApiService::class.java)
    }

    // -----------------------------
    // Spotify API
    // -----------------------------
    val spotifyApiService: SpotifyApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyApiService::class.java)
    }
}