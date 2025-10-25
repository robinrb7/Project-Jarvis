package com.example.projectjarvis.model

data class GrogRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Float = 1f,
    val max_completion_tokens: Int = 200,
    val top_p: Float = 1f,
    val stream: Boolean = false,
    val stop: String? = null
)
