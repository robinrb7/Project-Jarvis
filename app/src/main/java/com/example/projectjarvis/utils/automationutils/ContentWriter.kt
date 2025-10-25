package com.example.automation.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.projectjarvis.model.GrogRequest
import com.example.projectjarvis.model.Message
import com.example.projectjarvis.networking.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContentWriter(private val context: Context) {

    suspend fun generateContent(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val api = RetrofitClient.queryApiService("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")

            val systemPrompt = "Hello i am 'Robin Singh Khural'.You are a content writer .You write high-quality formal content like applications, letters, essays, or articles as requested by the user. Keep our answers around minimum of 150 words and maximum of 250 to 300 words."

            val messages = listOf(
                Message(role = "system", content = systemPrompt),
                Message(role = "user", content = prompt)
            )

            val request = GrogRequest(
                model = "llama-3.3-70b-versatile",
                messages = messages,
                temperature = 0.7f,
                max_completion_tokens = 2048
            )

            val response = try {
                api.getResponse(request)
            } catch (e: Exception) {
                return@withContext "API request failed: ${e.message}"
            }

            val content = response.choices?.firstOrNull()?.message?.content
                ?: return@withContext "No content received from API."

            openNotepadSafely(content)
            return@withContext content

        } catch (e: Exception) {
            return@withContext "Failed to generate content: ${e.message}"
        }
    }

    private fun openNotepadSafely(content: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "No app available to open content.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to open content: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
