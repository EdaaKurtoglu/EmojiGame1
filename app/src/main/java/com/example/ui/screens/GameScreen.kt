package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameMode
import com.example.data.model.GameStats
import com.example.ui.ActiveGameState
import com.example.ui.components.EmojiDisplayCard
import com.example.ui.components.GameOverDialog
import com.example.ui.components.GameTopBar
import com.example.ui.components.JokerBar
import com.example.ui.components.ResultDialog
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameGreen
import com.example.ui.theme.GameOrange
import com.example.ui.theme.GamePurple
import com.example.ui.theme.GamePurpleLight
import com.example.ui.theme.GameRed
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    gameState: ActiveGameState,
    stats: GameStats,
    onInputChange: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onLetterHint: () -> Unit,
    onEliminateHint: () -> Unit,
    onArtistHint: () -> Unit,
    onSkipQuestion: () -> Unit,
    onContinueGame: () -> Unit,
    onPlayAgain: () -> Unit,
    onToggleSound: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(gameState.isWrongAnswerEffect) {
        if (gameState.isWrongAnswerEffect) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -20f at 60
                    20f at 120
                    -16f at 180
                    16f at 240
                    -8f at 300
                    8f at 350
                    0f at 400
                }
            )
        }
    }

    val song = gameState.currentSong

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            GameTopBar(
                lives = gameState.lives,
                maxLives = 3,
                score = gameState.sessionScore,
                combo = gameState.sessionCombo,
                coins = stats.coins,
                isSoundEnabled = stats.soundEnabled,
                onToggleSound = onToggleSound,
                onBackClick = onGoHome,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Zamana Karşı (Time Attack) Countdown Timer Bar
            if (gameState.gameMode == GameMode.ZAMANA_KARSI) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (gameState.timeRemainingSeconds <= 10) GameRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = if (gameState.timeRemainingSeconds <= 10) GameRed else GameGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kalan Süre: ${gameState.timeRemainingSeconds} sn",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (gameState.timeRemainingSeconds <= 10) GameRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Interactive Content (wrapped in shake effect)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(x = shakeOffset.value.roundToInt(), y = 0) }
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (song != null) {
                    EmojiDisplayCard(
                        emojis = song.emojis,
                        category = song.category,
                        difficulty = song.difficulty
                    )
                } else {
                    // Fallback error / empty loading
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Şarkılar yükleniyor veya bu kategoride soru kalmadı...",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Answer Input Field
                OutlinedTextField(
                    value = gameState.currentInput,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("song_input_field"),
                    placeholder = {
                        Text(
                            text = "Şarkı adını yaz...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    trailingIcon = {
                        if (gameState.currentInput.isNotEmpty()) {
                            IconButton(onClick = { onInputChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Temizle",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onSubmitAnswer()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (gameState.isWrongAnswerEffect) GameRed else GamePurple,
                        unfocusedBorderColor = if (gameState.isWrongAnswerEffect) GameRed else MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Submit Button: "TAHMİN ET 🎯"
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSubmitAnswer()
                    },
                    enabled = gameState.currentInput.trim().isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = GamePurple.copy(alpha = 0.5f))
                        .testTag("submit_guess_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamePurple,
                        disabledContainerColor = GamePurple.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = "TAHMİN ET 🎯",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Jokers Section
                JokerBar(
                    playerCoins = stats.coins,
                    revealedFirstLetter = gameState.revealedFirstLetter,
                    revealedArtist = gameState.revealedArtist,
                    revealedLengthHint = gameState.revealedLengthHint,
                    onUseLetterHint = onLetterHint,
                    onUseEliminateHint = onEliminateHint,
                    onUseArtistHint = onArtistHint,
                    onSkipQuestion = onSkipQuestion
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Wrong Answer Animated Toast Feedback Banner
        AnimatedVisibility(
            visible = gameState.wrongFeedbackMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp, start = 24.dp, end = 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = GameRed,
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "❌", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = gameState.wrongFeedbackMessage ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Success Result Dialog
        if (song != null) {
            ResultDialog(
                isOpen = gameState.isResultDialogOpen,
                title = song.title,
                artist = song.artist,
                emojis = song.emojis,
                lyricSnippet = song.lyricSnippet,
                pointsEarned = gameState.lastPointsEarned,
                coinsEarned = gameState.lastCoinsEarned,
                currentCombo = gameState.sessionCombo,
                onNextQuestion = onNextQuestion
            )
        }

        // Game Over Dialog
        GameOverDialog(
            isOpen = gameState.isGameOverDialogOpen,
            score = gameState.sessionScore,
            correctCount = gameState.sessionCorrect,
            wrongCount = gameState.sessionWrong,
            bestCombo = gameState.sessionBestCombo,
            onContinueGame = onContinueGame,
            onPlayAgain = onPlayAgain,
            onGoHome = onGoHome
        )
    }
}
