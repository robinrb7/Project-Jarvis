package com.example.projectjarvis.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectjarvis.model.Message
import com.example.projectjarvis.repository.*
import com.example.projectjarvis.utils.*
import com.example.projectjarvis.utils.automationutils.CommandProcessor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JarvisViewModel(
    private val context: Context,
    private val brainRepository: BrainRepository,
    private val generalRepository: GeneralRepository,
    private val realtimeRepository: RealtimeRepository,
    private val automationRepository: AutomationRepository,
    private val spotifyHelper: SpotifyHelper
) : ViewModel() {

    private val _jarvisResponse = MutableStateFlow("")
    val jarvisResponse: StateFlow<String> = _jarvisResponse

    /**
     * Main function to handle recognized text.
     */
    fun handleRecognizedText(query: String) {
        viewModelScope.launch {
            try {
                Log.i("JarvisFlow", "Processing user query: $query")

                // Step 1: Brain API classification
                val safeResponse = try {
                    brainRepository.classifyQuery(BRAIN_PREAMBLE, CHAT_HISTORY, query)?.trim() ?: ""
                } catch (e: Exception) {
                    Log.e("JarvisBrain", "Brain API failed: ${e.message}", e)
                    _jarvisResponse.value = "Failed to contact Brain API."
                    return@launch
                }

                Log.i("JarvisBrain", "Brain classification: $safeResponse")

                if (safeResponse.isBlank()) {
                    _jarvisResponse.value = "I couldn't understand that, sir."
                    return@launch
                }

                // Step 2: Validate recognized functions
                val valid = funcs.any { safeResponse.lowercase().startsWith(it) }
                if (!valid) {
                    _jarvisResponse.value = "Invalid classification received."
                    return@launch
                }

                // Step 3: Split multiple commands
                val separatedCommands = safeResponse.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                if (separatedCommands.isEmpty()) {
                    _jarvisResponse.value = "No actionable commands found."
                    return@launch
                }

                // Step 4: Execute commands asynchronously
                val responses = separatedCommands.map { command ->
                    async {
                        val parts = command.split(" ", limit = 2)
                        val handlerType = parts.getOrNull(0)?.lowercase() ?: "general"
                        val actualQuery = parts.getOrNull(1) ?: query

                        Log.i("JarvisRouter", "Handler: $handlerType | Query: $actualQuery | full response: $handlerType $actualQuery")

                        try {
                            when (handlerType) {
                                "general" -> handleGeneralQuery(actualQuery)
                                "realtime" -> handleRealtimeQuery(actualQuery)
                                else -> handleAutomationQuery(handlerType+" "+actualQuery)
                            }
                        } catch (e: Exception) {
                            Log.e("JarvisCommand", "Error in $handlerType handler: ${e.message}", e)
                            "Failed to execute $handlerType command."
                        }
                    }
                }.awaitAll()

                // Step 5: Combine responses
                val combinedResponse = responses.joinToString("\n\n")
                _jarvisResponse.value = combinedResponse

                // Step 6: Play TTS
                try {
                    playJarvisVoice(context, combinedResponse)
                } catch (e: Exception) {
                    Log.e("JarvisTTS", "Failed to play TTS: ${e.message}", e)
                }

            } catch (e: Exception) {
                Log.e("JarvisError", "Unexpected error: ${e.message}", e)
                _jarvisResponse.value = "An unexpected error occurred."
            }
        }
    }

    // -----------------------------
    // Handlers
    // -----------------------------
    private suspend fun handleGeneralQuery(query: String): String {
        return try {
            val systemPrompt = GeneralPreamble.getSystemContext("Robin Singh Khural", "Jarvis")
            val realtimeInfo = RealTimeInfo.getSystemTime()
            generalRepository.askQuestion(systemPrompt, realtimeInfo, query)
        } catch (e: Exception) {
            Log.e("JarvisGeneral", "Error: ${e.message}", e)
            "Failed to process general query."
        }
    }

    private suspend fun handleRealtimeQuery(query: String): String {
        return try {
            val systemPrompt = RealtimePreamble.getSystemContext("Robin Singh Khural", "Jarvis")
            val realtimeInfo = RealTimeInfo.getSystemTime()
            val searchResults = try {
                GoogleSearch.getSearchResults(query)
            } catch (e: Exception) {
                Log.e("JarvisSearch", "Google search failed: ${e.message}", e)
                "No search results available."
            }

            val messages = listOf(
                Message("system", systemPrompt),
                Message("system", realtimeInfo),
                Message("system", searchResults),
                Message("user", query)
            )
            realtimeRepository.getAnswer(messages)
        } catch (e: Exception) {
            Log.e("JarvisRealtime", "Error: ${e.message}", e)
            "Failed to process realtime query."
        }
    }

    private suspend fun handleAutomationQuery(query: String): String {
        return try {
            val processor = CommandProcessor(
                automationRepository,
                context,
                spotifyHelper
            )
            processor.processCommand(query)
        } catch (e: Exception) {
            Log.e("JarvisAutomation", "Error: ${e.message}", e)
            "Failed to execute automation command."
        }
    }
}
