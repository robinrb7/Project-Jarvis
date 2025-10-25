package com.example.projectjarvis.utils

import android.content.Context
import android.util.Log
import com.example.projectjarvis.model.SearchResponse
import com.example.projectjarvis.networking.retrofitservices.SpotifyApiService
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SpotifyHelper(private val context: Context) {

    private val clientId = "acf77eb726c54f07b6a654c52b82fe35"
    private val redirectUri = "com.example.projectjarvis://callback"
    var spotifyAppRemote: SpotifyAppRemote? = null
    var spotifyAccessToken: String? = null

    fun setAccessToken(token: String) {
        spotifyAccessToken = token
        Log.d("SpotifyHelper", "Access token set")
    }

    fun connectSpotifyAppRemote(onConnected: () -> Unit) {
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                Log.d("SpotifyHelper", "App Remote connected!")
                onConnected()
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("SpotifyHelper", "App Remote connection failed: ${throwable.message}")
            }
        })
    }

    fun searchAndPlaySong(query: String) {
        val token = spotifyAccessToken ?: run {
            Log.e("SpotifyHelper", "No access token. Authenticate in activity first.")
            return
        }

        val cleanedQuery = simplifyQuery(query)
        Log.d("SpotifyHelper", "Cleaned query for Spotify API: '$cleanedQuery'")

        val encodedQuery = URLEncoder.encode(cleanedQuery, StandardCharsets.UTF_8.toString())

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(SpotifyApiService::class.java)
        val call = service.searchTrack("Bearer $token", encodedQuery)

        call.enqueue(object : retrofit2.Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: retrofit2.Response<SearchResponse>
            ) {
                val tracks = response.body()?.tracks?.items
                if (tracks.isNullOrEmpty()) {
                    Log.d("SpotifyHelper", "No tracks found for query: $cleanedQuery")
                    return
                }

                Log.d("SpotifyHelper", "Search results for '$cleanedQuery':")
                tracks.forEachIndexed { index, track ->
                    val artists = track.artists.joinToString { it.name }
                    Log.d("SpotifyHelper", "${index + 1}. ${track.name} by $artists (URI: ${track.uri})")
                }

                val trackUri = tracks.first().uri
                spotifyAppRemote?.playerApi?.play(trackUri)
                Log.d("SpotifyHelper", "Playing track: $trackUri")
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.e("SpotifyHelper", "Search failed: ${t.message}")
            }
        })
    }

    // Converts "boyfriend by karan aujila" → "boyfriend karan aujila"
    private fun simplifyQuery(query: String): String {
        return if (" by " in query.lowercase()) {
            val parts = query.split(" by ", limit = 2)
            val combined = "${parts[0].trim()} ${parts[1].trim()}"
            Log.d("SpotifyHelper", "Simplified Query: '$query' → '$combined'")
            combined
        } else {
            query.trim()
        }
    }


    //handle music functions
    fun handleMusicControl(command: String) {
        if (spotifyAppRemote == null) {
            Log.e("SpotifyHelper", "Spotify not connected")
            return
        }

        when {
            command.contains("pause", ignoreCase = true) ||
                    command.contains("stop", ignoreCase = true) -> {
                spotifyAppRemote?.playerApi?.pause()
                Log.d("SpotifyHelper", "Music paused/stopped")
            }

            command.contains("next") -> {
                spotifyAppRemote?.playerApi?.skipNext()?.setResultCallback {
                    Log.d("SpotifyHelper", "Successfully skipped next")
                }?.setErrorCallback { throwable ->
                    Log.e("SpotifyHelper", "Failed to skip next: ${throwable.message}")
                }
            }

            command.contains("previous") -> {
                spotifyAppRemote?.playerApi?.skipPrevious()?.setResultCallback {
                    Log.d("SpotifyHelper", "Successfully skipped previous")
                }?.setErrorCallback { throwable ->
                    Log.e("SpotifyHelper", "Failed to skip previous: ${throwable.message}")
                }
            }

            command.contains("resume") || command == "play" -> {
                spotifyAppRemote?.playerApi?.resume()
                Log.d("SpotifyHelper", "Music resumed")
            }

            command.contains("shuffle on") -> {
                spotifyAppRemote?.playerApi?.setShuffle(true)
                Log.d("SpotifyHelper", "Shuffle enabled")
            }

            command.contains("shuffle off") -> {
                spotifyAppRemote?.playerApi?.setShuffle(false)
                Log.d("SpotifyHelper", "Shuffle disabled")
            }

            // Track info
            command.contains("current song") || command.contains("what song") -> {
                spotifyAppRemote?.playerApi?.playerState?.setResultCallback { state ->
                    val track = state.track
                    if (track != null) {
                        Log.d("SpotifyHelper", "Now playing: ${track.name} by ${track.artist.name}")
                        // Optional: Use TTS here to announce
                    } else {
                        Log.d("SpotifyHelper", "No track currently playing")
                    }
                }
            }


            else -> {
                Log.d("SpotifyHelper", "Unknown music control command: $command")
            }
        }
    }


}