package com.example.youtubewrapped.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

// Light Mode Colors
private val LightColorScheme = lightColorScheme(
    primary = YouTubeRed,
    onPrimary = TextWhite,
    primaryContainer = LightGray,
    secondary = DarkRed,
    onSecondary = TextWhite,
    background = WhiteBackground,
    onBackground = TextBlack,
    surface = LightGray,
    onSurface = TextBlack
)

// Dark Mode Colors
private val DarkColorScheme = darkColorScheme(
    primary = YouTubeRed,
    onPrimary = TextWhite,
    primaryContainer = DarkGray,
    secondary = LightRed,
    onSecondary = TextBlack,
    background = BlackBackground,
    onBackground = TextWhite,
    surface = DarkGray,
    onSurface = TextWhite
)

@Composable
fun YoutubeWrappedTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
