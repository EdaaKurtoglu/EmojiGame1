package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GameCyan
import com.example.ui.theme.GameGold
import com.example.ui.theme.GameGreen
import com.example.ui.theme.GameOrange
import com.example.ui.theme.GamePurpleLight

@Composable
fun JokerBar(
    playerCoins: Int,
    revealedFirstLetter: String?,
    revealedArtist: String?,
    revealedLengthHint: String?,
    onUseLetterHint: () -> Unit,
    onUseEliminateHint: () -> Unit,
    onUseArtistHint: () -> Unit,
    onSkipQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Active Revealed Clues Box (if any joker was used)
        val hasAnyClue = !revealedFirstLetter.isNullOrEmpty() ||
                !revealedArtist.isNullOrEmpty() ||
                !revealedLengthHint.isNullOrEmpty()

        AnimatedVisibility(
            visible = hasAnyClue,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("revealed_hints_box"),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    revealedFirstLetter?.let { letter ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "İlk Harf: ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = letter,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Black,
                                color = GameGold
                            )
                        }
                    }

                    revealedArtist?.let { artist ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎤", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sanatçı: ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = artist,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    revealedLengthHint?.let { hint ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✂️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "İpucu: ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = hint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Jokers Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Harf İpucu Joker
            JokerActionButton(
                icon = "💡",
                label = "Harf Aç",
                cost = 50,
                isAffordable = playerCoins >= 50,
                isUsed = revealedFirstLetter != null,
                onClick = onUseLetterHint,
                testTag = "joker_letter_button",
                modifier = Modifier.weight(1f)
            )

            // Harf Sil / Boyut İpucu Joker
            JokerActionButton(
                icon = "✂️",
                label = "Harf Sil",
                cost = 75,
                isAffordable = playerCoins >= 75,
                isUsed = revealedLengthHint != null,
                onClick = onUseEliminateHint,
                testTag = "joker_eliminate_button",
                modifier = Modifier.weight(1f)
            )

            // Şarkıcıyı Göster Joker
            JokerActionButton(
                icon = "🎤",
                label = "Sanatçı",
                cost = 100,
                isAffordable = playerCoins >= 100,
                isUsed = revealedArtist != null,
                onClick = onUseArtistHint,
                testTag = "joker_artist_button",
                modifier = Modifier.weight(1f)
            )

            // Değiştir (Skip)
            JokerActionButton(
                icon = "🔄",
                label = "Değiştir",
                cost = 50,
                isAffordable = playerCoins >= 50,
                isUsed = false,
                onClick = onSkipQuestion,
                testTag = "joker_skip_button",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun JokerActionButton(
    icon: String,
    label: String,
    cost: Int,
    isAffordable: Boolean,
    isUsed: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val alpha = if (isUsed || !isAffordable) 0.5f else 1f
    val bgColor = if (isUsed) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isUsed) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isUsed && isAffordable, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🪙",
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = if (isUsed) "Açık" else "$cost",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUsed) GameGreen else if (isAffordable) GameGold else Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}
