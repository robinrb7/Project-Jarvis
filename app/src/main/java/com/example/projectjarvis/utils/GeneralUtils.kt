package com.example.projectjarvis.utils

object AnswerModifier {
    fun cleanResponse(response: String): String {
        return response
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }
}

object GeneralPreamble {
    fun getSystemContext(username: String, assistantName: String): String {
        return """
            Hello, I am $username. I have built you, you will always respect me and designate me with 'Sir' at start of the conversation only and never on the end.
            You are a very accurate and advanced AI assistant named $assistantName which also has real-time up-to-date information from the internet.
            *** Do not tell time until I ask, do not talk too much, just answer the question. ***
            *** Reply in only English, even if the question is in Hindi, reply in English. ***
            *** Do not provide notes in the output, just answer the question and never mention your training data. ***
        """.trimIndent()
    }
}

object RealTimeInfo {
    fun getSystemTime(): String {
        val now = java.util.Date()
        val format = java.text.SimpleDateFormat("EEEE, MMMM dd, yyyy HH:mm:ss", java.util.Locale.getDefault())
        return " Use this information if you want to: Current date and time: ${format.format(now)}"
    }
}