package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GamePurpleLight,
    onPrimary = Color.White,
    primaryContainer = GamePurpleDark,
    onPrimaryContainer = Color.White,
    secondary = GameGold,
    onSecondary = Color.Black,
    secondaryContainer = GameGoldDark,
    onSecondaryContainer = Color.White,
    tertiary = GamePink,
    onTertiary = Color.White,
    background = GameDarkBackground,
    onBackground = GameDarkText,
    surface = GameDarkSurface,
    onSurface = GameDarkText,
    surfaceVariant = GameDarkSurfaceVariant,
    onSurfaceVariant = GameDarkTextSecondary,
    error = GameRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = GamePurple,
    onPrimary = Color.White,
    primaryContainer = GamePurpleLight,
    onPrimaryContainer = Color.White,
    secondary = GameGold,
    onSecondary = Color.Black,
    secondaryContainer = GameGoldLight,
    onSecondaryContainer = Color.Black,
    tertiary = GamePink,
    onTertiary = Color.White,
    background = GameLightBackground,
    onBackground = GameLightText,
    surface = GameLightSurface,
    onSurface = GameLightText,
    surfaceVariant = GameLightSurfaceVariant,
    onSurfaceVariant = GameLightTextSecondary,
    error = GameRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
