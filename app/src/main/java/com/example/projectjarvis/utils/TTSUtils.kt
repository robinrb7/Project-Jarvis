package com.example.projectjarvis.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.projectjarvis.networking.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

suspend fun playJarvisVoice(context: Context, text: String, mediaPlayer: MediaPlayer?) {
    withContext(Dispatchers.IO) {
        try {
            // Use Retrofit suspend call or wrap execute() in withContext(IO)
            val response = RetrofitClient.ttsApiService.getSpeech(text)

            if (response.isSuccessful && response.body() != null) {
                val audioBytes = response.body()!!.bytes()
                val tempFile = File(context.cacheDir, "tts_current.mp3")
                tempFile.writeBytes(audioBytes)

                // Stop previous playback if any
                mediaPlayer?.stop()
                mediaPlayer?.release()

                val player = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    prepare()
                    start()
                }

                // Suspend until playback finishes
                while (player.isPlaying) {
                    kotlinx.coroutines.delay(100)
                }

                player.release()
            } else {
                Log.e("TTS", "Response failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("TTS", "Playback error: ${e.message}")
        }
    }
}