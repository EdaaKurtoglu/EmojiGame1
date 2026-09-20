package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.DailySongScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ModeSelectScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val stats by viewModel.gameStats.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val gameState by viewModel.gameState.collectAsState()

            val isDarkTheme = stats.isDarkTheme

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // System back button handling
                        if (currentScreen != Screen.HOME) {
                            BackHandler {
                                viewModel.navigateTo(Screen.HOME)
                            }
                        }

                        when (currentScreen) {
                            Screen.HOME -> {
                                HomeScreen(
                                    stats = stats,
                                    isDailySolved = viewModel.isDailySongSolvedToday(),
                                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                                )
                            }

                            Screen.MODE_SELECT -> {
                                ModeSelectScreen(
                                    onStartGame = { mode, category, difficulty ->
                                        viewModel.startNewGame(mode, category, difficulty)
                                    },
                                    onBack = { viewModel.navigateTo(Screen.HOME) }
                                )
                            }

                            Screen.GAME -> {
                                GameScreen(
                                    gameState = gameState,
                                    stats = stats,
                                    onInputChange = { input -> viewModel.onInputChange(input) },
                                    onSubmitAnswer = { viewModel.submitAnswer() },
                                    onNextQuestion = { viewModel.nextQuestion() },
                                    onLetterHint = { viewModel.useLetterHint() },
                                    onEliminateHint = { viewModel.useEliminateHint() },
                                    onArtistHint = { viewModel.useArtistHint() },
                                    onSkipQuestion = { viewModel.skipQuestion() },
                                    onContinueGame = { viewModel.continueGameWithExtraLife() },
                                    onPlayAgain = {
                                        viewModel.startNewGame(
                                            gameState.gameMode,
                                            gameState.selectedCategory,
                                            gameState.selectedDifficulty
                                        )
                                    },
                                    onToggleSound = { viewModel.toggleSound() },
                                    onGoHome = { viewModel.navigateTo(Screen.HOME) }
                                )
                            }

                            Screen.DAILY_SONG -> {
                                val dailySong = viewModel.getDailySong()
                                val isDailySolved = viewModel.isDailySongSolvedToday()

                                DailySongScreen(
                                    dailySong = dailySong,
                                    isAlreadySolved = isDailySolved,
                                    onSubmitAnswer = { answer, song ->
                                        viewModel.submitDailyAnswer(answer, song)
                                    },
                                    onBack = { viewModel.navigateTo(Screen.HOME) }
                                )
                            }

                            Screen.ACHIEVEMENTS -> {
                                AchievementsScreen(
                                    achievements = viewModel.getAchievements(),
                                    onBack = { viewModel.navigateTo(Screen.HOME) }
                                )
                            }

                            Screen.STATISTICS -> {
                                StatisticsScreen(
                                    stats = stats,
                                    onBack = { viewModel.navigateTo(Screen.HOME) }
                                )
                            }

                            Screen.SETTINGS -> {
                                SettingsScreen(
                                    stats = stats,
                                    onToggleSound = { viewModel.toggleSound() },
                                    onToggleVibration = { viewModel.toggleVibration() },
                                    onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                                    onResetProgress = { viewModel.resetAllProgress() },
                                    onBack = { viewModel.navigateTo(Screen.HOME) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
