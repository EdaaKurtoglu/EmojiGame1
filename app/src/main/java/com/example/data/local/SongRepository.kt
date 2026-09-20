package com.example.data.local

import android.content.Context
import android.util.Log
import com.example.data.model.Difficulty
import com.example.data.model.Song
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SongRepository(private val context: Context) {

    private var cachedSongs: List<Song> = emptyList()
    private var isLoaded = false

    fun loadSongs(): Result<List<Song>> {
        if (isLoaded && cachedSongs.isNotEmpty()) {
            return Result.success(cachedSongs)
        }

        return try {
            val jsonString = context.assets.open("songs.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val songsList = mutableListOf<Song>()
            val seenIds = mutableSetOf<Int>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                val id = obj.optInt("id", -1)
                if (id <= 0 || seenIds.contains(id)) continue

                val title = obj.optString("title", "").trim()
                val artist = obj.optString("artist", "").trim()
                if (title.isEmpty()) continue

                val emojisArray = obj.optJSONArray("emojis")
                val emojis = mutableListOf<String>()
                if (emojisArray != null) {
                    for (j in 0 until emojisArray.length()) {
                        emojis.add(emojisArray.optString(j))
                    }
                }
                if (emojis.isEmpty()) emojis.add("🎵")

                val acceptedAnswersArray = obj.optJSONArray("acceptedAnswers")
                val accepted = mutableListOf<String>()
                if (acceptedAnswersArray != null) {
                    for (j in 0 until acceptedAnswersArray.length()) {
                        val ans = acceptedAnswersArray.optString(j).trim()
                        if (ans.isNotEmpty()) accepted.add(ans)
                    }
                }
                if (!accepted.contains(title)) accepted.add(title)

                val difficultyStr = obj.optString("difficulty", "medium")
                val category = obj.optString("category", "Türkçe Pop")
                val hint = obj.optString("hint", "Şarkının adı ${title.length} karakterden oluşuyor.")
                val lyricSnippet = obj.optString("lyricSnippet", "")

                songsList.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        emojis = emojis,
                        lyricSnippet = lyricSnippet,
                        acceptedAnswers = accepted,
                        difficulty = Difficulty.fromKey(difficultyStr),
                        category = category,
                        hint = hint
                    )
                )
                seenIds.add(id)
            }

            cachedSongs = songsList
            isLoaded = true
            Log.d("SongRepository", "Loaded ${cachedSongs.size} songs from assets.")
            Result.success(cachedSongs)
        } catch (e: Exception) {
            Log.e("SongRepository", "Failed to load songs.json", e)
            Result.failure(e)
        }
    }

    fun getAllSongs(): List<Song> {
        if (!isLoaded) loadSongs()
        return cachedSongs
    }

    fun getSongs(
        categoryName: String? = null,
        difficulty: Difficulty? = null
    ): List<Song> {
        var list = getAllSongs()

        if (!categoryName.isNullOrEmpty() && !categoryName.equals("Karışık", ignoreCase = true) && !categoryName.equals("all", ignoreCase = true)) {
            list = list.filter { it.category.equals(categoryName, ignoreCase = true) }
        }

        if (difficulty != null) {
            list = list.filter { it.difficulty == difficulty }
        }

        return list.ifEmpty { getAllSongs() }
    }

    /**
     * Deterministically picks a daily song based on the calendar date (YYYY-MM-DD).
     * Everyone on the same day gets the same song.
     */
    fun getDailySong(date: Date = Date()): Song? {
        val all = getAllSongs()
        if (all.isEmpty()) return null

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
        val dateString = format.format(date)

        var hash = 0
        for (char in dateString) {
            hash = (hash * 31 + char.code) and 0x7FFFFFFF
        }

        val index = hash % all.size
        return all[index]
    }

    fun getTodayDateString(date: Date = Date()): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(date)
    }
}
