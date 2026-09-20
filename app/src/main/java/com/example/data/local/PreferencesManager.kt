package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.GameStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("emoji_song_game_prefs", Context.MODE_PRIVATE)

    private val _gameStats = MutableStateFlow(loadStats())
    val gameStats: StateFlow<GameStats> = _gameStats.asStateFlow()

    private fun loadStats(): GameStats {
        val coins = prefs.getInt(KEY_COINS, 500)
        val totalScore = prefs.getInt(KEY_TOTAL_SCORE, 0)
        val bestScore = prefs.getInt(KEY_BEST_SCORE, 0)
        val currentCombo = prefs.getInt(KEY_CURRENT_COMBO, 0)
        val bestCombo = prefs.getInt(KEY_BEST_COMBO, 0)
        val totalSolved = prefs.getInt(KEY_TOTAL_SOLVED, 0)
        val correctAnswers = prefs.getInt(KEY_CORRECT_ANSWERS, 0)
        val wrongAnswers = prefs.getInt(KEY_WRONG_ANSWERS, 0)
        val totalEarnedCoins = prefs.getInt(KEY_TOTAL_EARNED_COINS, 0)
        val soundEnabled = prefs.getBoolean(KEY_SOUND, true)
        val vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true)
        val isDarkTheme = prefs.getBoolean(KEY_DARK_THEME, true)
        val solvedIdsStr = prefs.getStringSet(KEY_SOLVED_IDS, emptySet()) ?: emptySet()
        val solvedIds = solvedIdsStr.mapNotNull { it.toIntOrNull() }.toSet()
        val achievements = prefs.getStringSet(KEY_ACHIEVEMENTS, emptySet()) ?: emptySet()
        val lastDailySolved = prefs.getString(KEY_DAILY_DATE, "") ?: ""

        return GameStats(
            coins = coins,
            totalScore = totalScore,
            bestScore = bestScore,
            currentCombo = currentCombo,
            bestCombo = bestCombo,
            totalSolved = totalSolved,
            correctAnswers = correctAnswers,
            wrongAnswers = wrongAnswers,
            totalEarnedCoins = totalEarnedCoins,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled,
            isDarkTheme = isDarkTheme,
            solvedSongIds = solvedIds,
            unlockedAchievementIds = achievements,
            lastDailySolvedDate = lastDailySolved
        )
    }

    fun addCoins(amount: Int) {
        val newCoins = (_gameStats.value.coins + amount).coerceAtLeast(0)
        val newTotalEarned = _gameStats.value.totalEarnedCoins + if (amount > 0) amount else 0
        prefs.edit()
            .putInt(KEY_COINS, newCoins)
            .putInt(KEY_TOTAL_EARNED_COINS, newTotalEarned)
            .apply()

        _gameStats.update {
            it.copy(coins = newCoins, totalEarnedCoins = newTotalEarned)
        }
    }

    fun spendCoins(amount: Int): Boolean {
        if (_gameStats.value.coins < amount) return false
        val newCoins = _gameStats.value.coins - amount
        prefs.edit().putInt(KEY_COINS, newCoins).apply()
        _gameStats.update { it.copy(coins = newCoins) }
        return true
    }

    fun recordCorrectAnswer(
        songId: Int,
        points: Int,
        coinsEarned: Int,
        newCombo: Int
    ) {
        val current = _gameStats.value
        val newSolved = current.solvedSongIds + songId
        val newScore = current.totalScore + points
        val newBestScore = maxOf(current.bestScore, newScore)
        val newBestCombo = maxOf(current.bestCombo, newCombo)
        val newCorrect = current.correctAnswers + 1
        val newTotalSolved = current.totalSolved + 1
        val newCoins = current.coins + coinsEarned
        val newTotalCoinsEarned = current.totalEarnedCoins + coinsEarned

        prefs.edit()
            .putInt(KEY_TOTAL_SCORE, newScore)
            .putInt(KEY_BEST_SCORE, newBestScore)
            .putInt(KEY_CURRENT_COMBO, newCombo)
            .putInt(KEY_BEST_COMBO, newBestCombo)
            .putInt(KEY_CORRECT_ANSWERS, newCorrect)
            .putInt(KEY_TOTAL_SOLVED, newTotalSolved)
            .putInt(KEY_COINS, newCoins)
            .putInt(KEY_TOTAL_EARNED_COINS, newTotalCoinsEarned)
            .putStringSet(KEY_SOLVED_IDS, newSolved.map { it.toString() }.toSet())
            .apply()

        _gameStats.update {
            it.copy(
                totalScore = newScore,
                bestScore = newBestScore,
                currentCombo = newCombo,
                bestCombo = newBestCombo,
                correctAnswers = newCorrect,
                totalSolved = newTotalSolved,
                coins = newCoins,
                totalEarnedCoins = newTotalCoinsEarned,
                solvedSongIds = newSolved
            )
        }
    }

    fun recordWrongAnswer() {
        val current = _gameStats.value
        val newWrong = current.wrongAnswers + 1
        prefs.edit()
            .putInt(KEY_WRONG_ANSWERS, newWrong)
            .putInt(KEY_CURRENT_COMBO, 0)
            .apply()

        _gameStats.update {
            it.copy(
                wrongAnswers = newWrong,
                currentCombo = 0
            )
        }
    }

    fun resetCombo() {
        prefs.edit().putInt(KEY_CURRENT_COMBO, 0).apply()
        _gameStats.update { it.copy(currentCombo = 0) }
    }

    fun setDailySolved(dateStr: String) {
        prefs.edit().putString(KEY_DAILY_DATE, dateStr).apply()
        _gameStats.update { it.copy(lastDailySolvedDate = dateStr) }
    }

    fun unlockAchievement(achievementId: String) {
        val newSet = _gameStats.value.unlockedAchievementIds + achievementId
        prefs.edit().putStringSet(KEY_ACHIEVEMENTS, newSet).apply()
        _gameStats.update { it.copy(unlockedAchievementIds = newSet) }
    }

    fun updateSettings(sound: Boolean? = null, vibration: Boolean? = null, darkTheme: Boolean? = null) {
        val editor = prefs.edit()
        var current = _gameStats.value

        sound?.let {
            editor.putBoolean(KEY_SOUND, it)
            current = current.copy(soundEnabled = it)
        }
        vibration?.let {
            editor.putBoolean(KEY_VIBRATION, it)
            current = current.copy(vibrationEnabled = it)
        }
        darkTheme?.let {
            editor.putBoolean(KEY_DARK_THEME, it)
            current = current.copy(isDarkTheme = it)
        }

        editor.apply()
        _gameStats.value = current
    }

    fun resetAllProgress() {
        prefs.edit().clear().apply()
        _gameStats.value = GameStats(coins = 500)
    }

    companion object {
        private const val KEY_COINS = "key_coins"
        private const val KEY_TOTAL_SCORE = "key_total_score"
        private const val KEY_BEST_SCORE = "key_best_score"
        private const val KEY_CURRENT_COMBO = "key_current_combo"
        private const val KEY_BEST_COMBO = "key_best_combo"
        private const val KEY_TOTAL_SOLVED = "key_total_solved"
        private const val KEY_CORRECT_ANSWERS = "key_correct_answers"
        private const val KEY_WRONG_ANSWERS = "key_wrong_answers"
        private const val KEY_TOTAL_EARNED_COINS = "key_total_earned_coins"
        private const val KEY_SOUND = "key_sound"
        private const val KEY_VIBRATION = "key_vibration"
        private const val KEY_DARK_THEME = "key_dark_theme"
        private const val KEY_SOLVED_IDS = "key_solved_ids"
        private const val KEY_ACHIEVEMENTS = "key_achievements"
        private const val KEY_DAILY_DATE = "key_daily_date"
    }
}
