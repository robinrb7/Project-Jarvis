package com.example.projectjarvis.utils.automationutils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.projectjarvis.repository.AutomationRepository
import com.example.projectjarvis.utils.SpotifyHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommandProcessor(
    private val repository: AutomationRepository,
    private val context: Context,
    private val spotifyHelper: SpotifyHelper
) {
    private val executor = AutomationExecutor(context)

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun processCommand(command: String): String = withContext(Dispatchers.IO) {
        val cmd = command.lowercase().trim()

        try {
            return@withContext when {
                cmd.startsWith("open ") -> {
                    safeExecute { executor.openApp(cmd.removePrefix("open ").trim()) }
                    "Opening ${cmd.removePrefix("open ")}..."
                }

                cmd.startsWith("close ") -> {
                    if (hasPermission(Manifest.permission.KILL_BACKGROUND_PROCESSES)) {
                        safeExecute { executor.closeApp(cmd.removePrefix("close ").trim()) }
                        "Closing ${cmd.removePrefix("close ")}..."
                    } else "Permission required to close apps."
                }

                (cmd.startsWith("play ") || cmd.startsWith("spotify search ")) -> {
                    val query = if (cmd.startsWith("play ")) {
                        cmd.removePrefix("play ").trim()
                    } else {
                        cmd.removePrefix("spotify search ").trim()
                    }

                    if (spotifyHelper.spotifyAccessToken == null) {
                        return@withContext "Spotify not authenticated. Authenticate in the app first."
                    }

                    safeExecute { spotifyHelper.searchAndPlaySong(query) }
                    "Playing \"$query\" on Spotify"
                }


                cmd.startsWith("google search ") -> {
                    safeExecute { executor.searchOnGoogle(cmd.removePrefix("google search ").trim()) }
                    "Searching on Google..."
                }

                cmd.startsWith("youtube search ") || cmd.startsWith("play ")-> {
                    safeExecute { executor.searchYoutube(cmd.removePrefix("youtube search ").trim()) }
                    "Searching on YouTube..."
                }

                cmd.startsWith("system ") && (
                        cmd.contains("pause") || cmd.contains("resume") ||
                                cmd.contains("stop") || cmd.contains("next") ||
                                cmd.contains("previous") || cmd.contains("shuffle") ||
                                cmd.contains("which song") || cmd.contains("what song")
                        ) -> {
                    val musicCommand = cmd.removePrefix("system ").trim()
                    safeExecute { spotifyHelper.handleMusicControl(musicCommand) }
                    "Executed music control: $musicCommand"
                }

                cmd.contains("system ") -> {
                    safeExecute { executor.systemControls(cmd.removePrefix("system ").trim()) }
                    "System Controls updated"
                }

                cmd.startsWith("content ")  -> {
                    safeExecute {
                        repository.writeContent(cmd.removePrefix("content ").trim())
                    }?.let { content ->
                        "Generated content:\n$content"
                    } ?: "Failed to generate content."
                }


                else -> {
                    "Command not recognized or delegated to general/realtime handler.\n $command"
                }
            }
        } catch (e: Exception) {
            return@withContext "Error executing command: ${e.message}"
        }
    }


    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun safeExecute(action: suspend () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            // Log error if needed
        }
    }
}
