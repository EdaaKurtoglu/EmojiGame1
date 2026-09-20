package com.example.data.model

enum class Difficulty(
    val key: String,
    val displayName: String,
    val basePoints: Int,
    val baseCoins: Int
) {
    EASY("easy", "Kolay", 100, 20),
    MEDIUM("medium", "Orta", 200, 30),
    HARD("hard", "Zor", 350, 50),
    VERY_HARD("very_hard", "Çok Zor", 500, 75);

    companion object {
        fun fromKey(key: String): Difficulty {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: MEDIUM
        }
    }
}
