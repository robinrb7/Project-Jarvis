package com.example.projectjarvis.networking.retrofitservices

import com.example.projectjarvis.model.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SpotifyApiService {
    @GET("v1/search?type=track&limit=1")
    fun searchTrack(
        @Header("Authorization") token: String,
        @Query("q") query: String
    ): Call<SearchResponse>
}