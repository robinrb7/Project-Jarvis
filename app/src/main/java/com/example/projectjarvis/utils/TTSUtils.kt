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

suspend fun playJarvisVoice(context: Context, text: String) {
    withContext(Dispatchers.IO) {
        val call = RetrofitClient.ttsApiService.getSpeech(text)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val audioBytes = response.body()!!.bytes()
                        val tempFile = File(context.cacheDir, "tts_current.mp3")
                        tempFile.writeBytes(audioBytes)

                        val player = MediaPlayer()
                        player.setDataSource(tempFile.absolutePath)
                        player.prepare()
                        player.start()

                    } catch (e: Exception) {
                        Log.e("TTS", "Playback error: ${e.message}")
                    }
                } else {
                    Log.e("TTS", "Response failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("TTS", "Error: ${t.message}")
            }
        })
    }
}