package com.example.projectjarvis.repository

import android.content.Context
import com.example.automation.utils.ContentWriter

class AutomationRepository(private val context: Context) {

    private val writer = ContentWriter(context)


    suspend fun writeContent(prompt: String): String {
        return try {
            writer.generateContent(prompt)
        } catch (e: Exception) {
            "Failed to generate content: ${e.message}"
        }
    }
}