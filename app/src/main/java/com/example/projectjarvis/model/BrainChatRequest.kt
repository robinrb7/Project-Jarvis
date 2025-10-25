package com.example.projectjarvis.model

data class BrainChatRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int = 100
)
