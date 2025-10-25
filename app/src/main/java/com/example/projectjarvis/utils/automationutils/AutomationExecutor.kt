package com.example.projectjarvis.utils.automationutils

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import com.example.projectjarvis.service.AutomationAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class AutomationExecutor(private val context: Context) {


    fun openApp(appName: String) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0)

        // Step 1: Try exact match on label
        val exactMatch = apps.firstOrNull {
            it.loadLabel(pm).toString().equals(appName, ignoreCase = true)
        }
        if (exactMatch != null) {
            startApp(exactMatch.activityInfo.packageName)
            return
        }

        // Step 2: Try partial match on label
        val partialMatch = apps.firstOrNull {
            it.loadLabel(pm).toString().contains(appName, ignoreCase = true)
        }
        if (partialMatch != null) {
            startApp(partialMatch.activityInfo.packageName)
            return
        }

        // Step 3: Fallback: Play Store search
        val playIntent = Intent(Intent.ACTION_VIEW)
        playIntent.data = Uri.parse("https://play.google.com/store/search?q=$appName&c=apps")
        context.startActivity(playIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun startApp(packageName: String) {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }



    fun closeApp(targetPackage: String) {
        val service = AutomationAccessibilityService.instance

        if (service != null) {
            // Try to close app only if it is foreground
            val closed = service.closeAppIfForeground(targetPackage)
            if (closed) {
                Log.d("AutomationExecutor", "$targetPackage was minimized successfully")
                return
            }
        }

        // Fallback: Accessibility Service not enabled OR target app not in foreground
        // Optionally notify user or just bring Jarvis to front
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        context.startActivity(launchIntent)

        Log.d("AutomationExecutor", "$targetPackage was not in foreground. Jarvis brought to front")
    }



    fun searchOnGoogle(query: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query"))
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

     suspend fun searchYoutube(query: String) = withContext(Dispatchers.IO)  {
        try {
            // 1️⃣ Prepare the search URL using YouTube Data API
            val apiKey = "AIzaSyAa9uawF8SHkUQic8MAvhhYou-wMkCTeqg"
            val encodedQuery = Uri.encode(query)
            val apiUrl =
                "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=1&q=$encodedQuery&key=$apiKey"

            // 2️⃣ Perform HTTP request
            val connection = URL(apiUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            // 3️⃣ Read response
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            // 4️⃣ Parse JSON to extract videoId
            val json = JSONObject(response)
            val items = json.getJSONArray("items")
            if (items.length() > 0) {
                val videoId =
                    items.getJSONObject(0).getJSONObject("id").getString("videoId")

                // 5️⃣ Build YouTube watch URL and open in app
                val videoUrl = "https://www.youtube.com/watch?v=$videoId"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)).apply {
                    setPackage("com.google.android.youtube") // Ensures it opens in app
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(intent)
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "No videos found for '$query'", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error playing video: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun systemControls(command: String){

        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        fun volumeUp() = adjustVolume(true)
        fun volumeDown() = adjustVolume(false)
        fun mute() = setVolumeLevel(0)
        fun unmute() = setVolumeLevel(100)

        when {
            command == "mute" -> mute()
            command == "unmute" -> unmute()
            command == "volume up" -> volumeUp()
            command == "volume down" -> volumeDown()

            command.startsWith("set volume to") -> {
                // Extract number from command
                val percent = Regex("\\d+").find(command)?.value?.toIntOrNull()
                if (percent != null) {
                    val level = (percent / 100.0 * maxVolume).toInt().coerceIn(0, maxVolume)
                    setVolumeLevel(level)
                }
            }
        }

    }

    private fun adjustVolume(up: Boolean) {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val dir = if (up) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, dir, AudioManager.FLAG_SHOW_UI)
    }

    private fun setVolumeLevel(level: Int) {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, level, AudioManager.FLAG_SHOW_UI)
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun toggleBluetooth(enable: Boolean) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (enable) adapter.enable() else adapter.disable()
    }

    fun toggleWiFi(enable: Boolean) {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifi.isWifiEnabled = enable
    }

//    fun toggleMobileData(enable: Boolean) {
//        // Restricted for Android 10+, open Internet Settings instead
//        val intent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
//        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
//    }

}