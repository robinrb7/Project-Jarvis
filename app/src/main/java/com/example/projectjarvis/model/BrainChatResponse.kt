package com.example.projectjarvis.model

data class BrainChatResponse(
    val id: String,
    val finish_reason: String,
    val message: MessageContentWrapper
)

data class MessageContentWrapper(
    val role: String,
    val content: List<ContentItem>
)

data class ContentItem(
    val type: String,
    val text: String
)
