package com.example.projectjarvis.model

data class GrogResponse(
    val choices: List<Choice>?
)

data class Choice(
    val message: Message?
)
