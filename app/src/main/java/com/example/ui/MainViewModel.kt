package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesManager
import com.example.data.local.SongRepository
import com.example.data.model.Achievement
import com.example.data.model.Difficulty
import com.example.data.model.GameCategory
import com.example.data.model.GameMode
import com.example.data.model.GameStats
import com.example.data.model.Song
import com.example.game.AnswerValidator
import com.example.game.SoundEffectManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    MODE_SELECT,
    GAME,
    DAILY_SONG,
    ACHIEVEMENTS,
    STATISTICS,
    SETTINGS
}

data class ActiveGameState(
    val currentSong: Song? = null,
    val gameMode: GameMode = GameMode.KLASIK,
    val selectedCategory: String = "Karışık",
    val selectedDifficulty: Difficulty? = null,
    val lives: Int = 3,
    val sessionScore: Int = 0,
    val sessionCorrect: Int = 0,
    val sessionWrong: Int = 0,
    val sessionCombo: Int = 0,
    val sessionBestCombo: Int = 0,
    val currentInput: String = "",
    val revealedFirstLetter: String? = null,
    val revealedArtist: String? = null,
    val revealedLengthHint: String? = null,
    val isResultDialogOpen: Boolean = false,
    val isGameOverDialogOpen: Boolean = false,
    val lastPointsEarned: Int = 0,
    val lastCoinsEarned: Int = 0,
    val isWrongAnswerEffect: Boolean = false,
    val wrongFeedbackMessage: String? = null,
    // Zamana Karşı (Time Attack)
    val timeRemainingSeconds: Int = 60,
    val isTimerRunning: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SongRepository(application)
    private val prefs = PreferencesManager(application)
    val soundManager = SoundEffectManager(application)

    val gameStats: StateFlow<GameStats> = prefs.gameStats

    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _gameState = MutableStateFlow(ActiveGameState())
    val gameState: StateFlow<ActiveGameState> = _gameState.asStateFlow()

    // Session-based played songs queue to avoid repeats
    private val sessionPlayedSongIds = mutableSetOf<Int>()
    private var timerJob: Job? = null

    init {
        repository.loadSongs()
        val stats = prefs.gameStats.value
        soundManager.isSoundEnabled = stats.soundEnabled
        soundManager.isVibrationEnabled = stats.vibrationEnabled
    }

    fun navigateTo(screen: Screen) {
        soundManager.playButtonClick()
        _currentScreen.value = screen
    }

    fun startNewGame(
        mode: GameMode = GameMode.KLASIK,
        category: String = "Karışık",
        difficulty: Difficulty? = null
    ) {
        soundManager.playButtonClick()
        sessionPlayedSongIds.clear()

        val initialLives = if (mode == GameMode.SERBEST_OYUN) 999 else 3
        val nextSong = pickNextSong(category, difficulty)

        _gameState.value = ActiveGameState(
            currentSong = nextSong,
            gameMode = mode,
            selectedCategory = category,
            selectedDifficulty = difficulty,
            lives = initialLives,
            sessionScore = 0,
            sessionCorrect = 0,
            sessionWrong = 0,
            sessionCombo = 0,
            sessionBestCombo = 0,
            currentInput = "",
            timeRemainingSeconds = 60,
            isTimerRunning = mode == GameMode.ZAMANA_KARSI
        )

        _currentScreen.value = Screen.GAME

        if (mode == GameMode.ZAMANA_KARSI) {
            startTimeAttackTimer()
        }
    }

    private fun startTimeAttackTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_gameState.value.timeRemainingSeconds > 0 && _gameState.value.isTimerRunning) {
                delay(1000)
                _gameState.update {
                    val newTime = it.timeRemainingSeconds - 1
                    if (newTime <= 0) {
                        soundManager.playGameOver()
                        it.copy(timeRemainingSeconds = 0, isGameOverDialogOpen = true, isTimerRunning = false)
                    } else {
                        it.copy(timeRemainingSeconds = newTime)
                    }
                }
            }
        }
    }

    fun onInputChange(input: String) {
        _gameState.update { it.copy(currentInput = input) }
    }

    fun submitAnswer() {
        val currentSong = _gameState.value.currentSong ?: return
        val userAnswer = _gameState.value.currentInput.trim()
        if (userAnswer.isEmpty()) return

        val isCorrect = AnswerValidator.isCorrect(
            userAnswer = userAnswer,
            acceptedAnswers = currentSong.acceptedAnswers,
            title = currentSong.title
        )

        if (isCorrect) {
            handleCorrectAnswer(currentSong)
        } else {
            handleWrongAnswer()
        }
    }

    private fun handleCorrectAnswer(song: Song) {
        val current = _gameState.value
        val newCombo = current.sessionCombo + 1
        val newBestCombo = maxOf(current.sessionBestCombo, newCombo)

        // Combo multiplier: 1 -> x1.0, 3 -> x1.5, 5 -> x2.0, 10 -> x3.0
        val multiplier = when {
            newCombo >= 10 -> 3.0
            newCombo >= 5 -> 2.0
            newCombo >= 3 -> 1.5
            else -> 1.0
        }

        val basePoints = song.difficulty.basePoints
        val pointsEarned = (basePoints * multiplier).toInt()

        val baseCoins = song.difficulty.baseCoins
        val comboBonusCoins = if (newCombo >= 3) (newCombo * 2) else 0
        val coinsEarned = baseCoins + comboBonusCoins

        val newSessionScore = current.sessionScore + pointsEarned
        val newCorrectCount = current.sessionCorrect + 1

        sessionPlayedSongIds.add(song.id)

        // Save progress to local preferences
        prefs.recordCorrectAnswer(
            songId = song.id,
            points = pointsEarned,
            coinsEarned = coinsEarned,
            newCombo = newCombo
        )

        // Audio & Haptic Feedback
        if (newCombo >= 3) {
            soundManager.playCombo(newCombo)
        } else {
            soundManager.playCorrect()
        }

        // Check and unlock achievements
        checkAchievements(newCorrectCount, newBestCombo)

        _gameState.update {
            it.copy(
                sessionScore = newSessionScore,
                sessionCorrect = newCorrectCount,
                sessionCombo = newCombo,
                sessionBestCombo = newBestCombo,
                lastPointsEarned = pointsEarned,
                lastCoinsEarned = coinsEarned,
                isResultDialogOpen = true,
                currentInput = ""
            )
        }
    }

    private fun handleWrongAnswer() {
        val current = _gameState.value
        val newWrong = current.sessionWrong + 1
        val newLives = if (current.gameMode == GameMode.SERBEST_OYUN) current.lives else current.lives - 1

        soundManager.playWrong()
        prefs.recordWrongAnswer()

        viewModelScope.launch {
            _gameState.update {
                it.copy(
                    lives = newLives,
                    sessionCombo = 0,
                    sessionWrong = newWrong,
                    isWrongAnswerEffect = true,
                    wrongFeedbackMessage = if (newLives <= 0) "Yanlış cevap! Canın bitti." else "Yanlış cevap! -1 ❤️"
                )
            }
            delay(1200)
            _gameState.update {
                it.copy(
                    isWrongAnswerEffect = false,
                    wrongFeedbackMessage = null,
                    isGameOverDialogOpen = newLives <= 0
                )
            }
            if (newLives <= 0) {
                soundManager.playGameOver()
            }
        }
    }

    fun nextQuestion() {
        soundManager.playButtonClick()
        val current = _gameState.value
        val next = pickNextSong(current.selectedCategory, current.selectedDifficulty)

        _gameState.update {
            it.copy(
                currentSong = next,
                isResultDialogOpen = false,
                currentInput = "",
                revealedFirstLetter = null,
                revealedArtist = null,
                revealedLengthHint = null
            )
        }
    }

    // Jokers
    fun useLetterHint() {
        val currentSong = _gameState.value.currentSong ?: return
        if (_gameState.value.revealedFirstLetter != null) return

        if (prefs.spendCoins(50)) {
            soundManager.playJoker()
            val firstLetter = currentSong.title.firstOrNull()?.toString()?.uppercase() ?: "?"
            _gameState.update {
                it.copy(revealedFirstLetter = "$firstLetter...")
            }
        }
    }

    fun useEliminateHint() {
        val currentSong = _gameState.value.currentSong ?: return
        if (_gameState.value.revealedLengthHint != null) return

        if (prefs.spendCoins(75)) {
            soundManager.playJoker()
            val cleanTitle = currentSong.title.replace(" ", "")
            val hintText = "Şarkının adı ${currentSong.title.split(" ").size} kelime ve ${cleanTitle.length} harften oluşuyor."
            _gameState.update {
                it.copy(revealedLengthHint = hintText)
            }
        }
    }

    fun useArtistHint() {
        val currentSong = _gameState.value.currentSong ?: return
        if (_gameState.value.revealedArtist != null) return

        if (prefs.spendCoins(100)) {
            soundManager.playJoker()
            _gameState.update {
                it.copy(revealedArtist = currentSong.artist)
            }
        }
    }

    fun skipQuestion() {
        val current = _gameState.value
        if (prefs.spendCoins(50)) {
            soundManager.playButtonClick()
            val next = pickNextSong(current.selectedCategory, current.selectedDifficulty)
            _gameState.update {
                it.copy(
                    currentSong = next,
                    currentInput = "",
                    revealedFirstLetter = null,
                    revealedArtist = null,
                    revealedLengthHint = null
                )
            }
        }
    }

    // Continue after Game Over (placeholder for rewarded ads)
    fun continueGameWithExtraLife() {
        soundManager.playSuccessFanfare()
        _gameState.update {
            it.copy(
                lives = 1,
                isGameOverDialogOpen = false,
                isTimerRunning = it.gameMode == GameMode.ZAMANA_KARSI,
                timeRemainingSeconds = if (it.gameMode == GameMode.ZAMANA_KARSI) it.timeRemainingSeconds + 30 else it.timeRemainingSeconds
            )
        }
        if (_gameState.value.gameMode == GameMode.ZAMANA_KARSI) {
            startTimeAttackTimer()
        }
    }

    private fun pickNextSong(category: String, difficulty: Difficulty?): Song? {
        val allFiltered = repository.getSongs(category, difficulty)
        if (allFiltered.isEmpty()) return repository.getAllSongs().firstOrNull()

        // Filter out songs already played this session
        val unplayed = allFiltered.filterNot { sessionPlayedSongIds.contains(it.id) }
        val pool = unplayed.ifEmpty {
            sessionPlayedSongIds.clear()
            allFiltered
        }

        return pool.shuffled().firstOrNull()
    }

    // Daily Song Flow
    fun getDailySong(): Song? = repository.getDailySong()

    fun isDailySongSolvedToday(): Boolean {
        val today = repository.getTodayDateString()
        return prefs.gameStats.value.lastDailySolvedDate == today
    }

    fun submitDailyAnswer(answer: String, song: Song): Boolean {
        val isCorrect = AnswerValidator.isCorrect(
            userAnswer = answer,
            acceptedAnswers = song.acceptedAnswers,
            title = song.title
        )

        if (isCorrect) {
            val today = repository.getTodayDateString()
            prefs.setDailySolved(today)
            prefs.recordCorrectAnswer(
                songId = song.id,
                points = 500,
                coinsEarned = 100,
                newCombo = prefs.gameStats.value.currentCombo + 1
            )
            prefs.unlockAchievement("ach_daily")
            soundManager.playSuccessFanfare()
            return true
        } else {
            soundManager.playWrong()
            return false
        }
    }

    // Achievements calculation
    fun getAchievements(): List<Achievement> {
        val stats = prefs.gameStats.value
        val solvedCount = stats.solvedSongIds.size
        val unlocked = stats.unlockedAchievementIds

        return listOf(
            Achievement(
                id = "ach_1",
                title = "İlk Adım",
                description = "İlk şarkını doğru tahmin et.",
                icon = "🏆",
                target = 1,
                currentProgress = solvedCount.coerceAtMost(1),
                isUnlocked = unlocked.contains("ach_1") || solvedCount >= 1,
                rewardCoins = 50
            ),
            Achievement(
                id = "ach_10",
                title = "Müzik Sever",
                description = "10 şarkıyı başarıyla çöz.",
                icon = "🎵",
                target = 10,
                currentProgress = solvedCount.coerceAtMost(10),
                isUnlocked = unlocked.contains("ach_10") || solvedCount >= 10,
                rewardCoins = 150
            ),
            Achievement(
                id = "ach_50",
                title = "Emoji Ustası",
                description = "50 şarkıyı emojilerden bil.",
                icon = "⭐",
                target = 50,
                currentProgress = solvedCount.coerceAtMost(50),
                isUnlocked = unlocked.contains("ach_50") || solvedCount >= 50,
                rewardCoins = 300
            ),
            Achievement(
                id = "ach_100",
                title = "Dedektif",
                description = "100 şarkının gizemini çöz.",
                icon = "🕵️",
                target = 100,
                currentProgress = solvedCount.coerceAtMost(100),
                isUnlocked = unlocked.contains("ach_100") || solvedCount >= 100,
                rewardCoins = 500
            ),
            Achievement(
                id = "ach_combo",
                title = "Combo Canavarı",
                description = "Arka arkaya 10 doğru cevap ver.",
                icon = "🔥",
                target = 10,
                currentProgress = stats.bestCombo.coerceAtMost(10),
                isUnlocked = unlocked.contains("ach_combo") || stats.bestCombo >= 10,
                rewardCoins = 250
            ),
            Achievement(
                id = "ach_rich",
                title = "Zengin",
                description = "Toplam 5000 coin topla.",
                icon = "💰",
                target = 5000,
                currentProgress = stats.totalEarnedCoins.coerceAtMost(5000),
                isUnlocked = unlocked.contains("ach_rich") || stats.totalEarnedCoins >= 5000,
                rewardCoins = 400
            ),
            Achievement(
                id = "ach_daily",
                title = "Günün Yıldızı",
                description = "Günün şarkısını doğru tahmin et.",
                icon = "🌟",
                target = 1,
                currentProgress = if (stats.lastDailySolvedDate.isNotEmpty()) 1 else 0,
                isUnlocked = unlocked.contains("ach_daily") || stats.lastDailySolvedDate.isNotEmpty(),
                rewardCoins = 100
            )
        )
    }

    private fun checkAchievements(sessionCorrect: Int, bestCombo: Int) {
        val stats = prefs.gameStats.value
        val solvedCount = stats.solvedSongIds.size

        if (solvedCount >= 1) prefs.unlockAchievement("ach_1")
        if (solvedCount >= 10) prefs.unlockAchievement("ach_10")
        if (solvedCount >= 50) prefs.unlockAchievement("ach_50")
        if (solvedCount >= 100) prefs.unlockAchievement("ach_100")
        if (bestCombo >= 10) prefs.unlockAchievement("ach_combo")
        if (stats.totalEarnedCoins >= 5000) prefs.unlockAchievement("ach_rich")
    }

    // Settings
    fun toggleSound() {
        val current = prefs.gameStats.value.soundEnabled
        prefs.updateSettings(sound = !current)
        soundManager.isSoundEnabled = !current
        soundManager.playButtonClick()
    }

    fun toggleVibration() {
        val current = prefs.gameStats.value.vibrationEnabled
        prefs.updateSettings(vibration = !current)
        soundManager.isVibrationEnabled = !current
        soundManager.playButtonClick()
    }

    fun toggleDarkTheme() {
        val current = prefs.gameStats.value.isDarkTheme
        prefs.updateSettings(darkTheme = !current)
        soundManager.playButtonClick()
    }

    fun resetAllProgress() {
        prefs.resetAllProgress()
        soundManager.playButtonClick()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundManager.release()
    }
}
