package com.example.data.model

data class Song(
    val id: Int,
    val title: String,
    val artist: String,
    val emojis: List<String>,
    val lyricSnippet: String = "", // Şarkının emojilerle anlatılan meşhur sözü
    val acceptedAnswers: List<String>,
    val difficulty: Difficulty,
    val category: String,
    val hint: String
)
