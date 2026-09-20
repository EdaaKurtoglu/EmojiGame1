package com.example.data.model

data class GameStats(
    val coins: Int = 500,
    val totalScore: Int = 0,
    val bestScore: Int = 0,
    val currentCombo: Int = 0,
    val bestCombo: Int = 0,
    val totalSolved: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val totalEarnedCoins: Int = 0,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val isDarkTheme: Boolean = true,
    val solvedSongIds: Set<Int> = emptySet(),
    val unlockedAchievementIds: Set<String> = emptySet(),
    val lastDailySolvedDate: String = ""
) {
    val winRatePercent: Int
        get() {
            val total = correctAnswers + wrongAnswers
            return if (total > 0) ((correctAnswers.toDouble() / total) * 100).toInt() else 0
        }
}
