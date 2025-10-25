package com.example.projectjarvis.model

data class TrackItem(
    val name: String,
    val uri: String,
    val artists: List<Artist>
)

data class Artist(
    val name: String
)

data class Tracks(
    val items: List<TrackItem>
)

data class SearchResponse(
    val tracks: Tracks
)
