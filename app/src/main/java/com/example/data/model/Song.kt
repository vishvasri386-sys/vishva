package com.example.data.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationText: String,
    val durationMs: Long,
    val audioUrl: String,
    val coverUrl: String,
    val isTrending: Boolean = false,
    val playlistCategory: String = "Neon Chill"
)
