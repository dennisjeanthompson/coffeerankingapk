package com.example.coffeerankingapk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Design tokens - exact hex colors
val BgCream = Color(0xFFF6F0E9)
val CardBg = Color(0xFFFFFFFF)
val PrimaryBrown = Color(0xFF6B3E2A)
val AccentLight = Color(0xFFE9DCCF)
val TextPrimary = Color(0xFF2C2C2C)
val TextMuted = Color(0xFF7A6F66)
val Success = Color(0xFF2EAA4F)
val Danger = Color(0xFFE35D4F)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBrown,
    secondary = AccentLight,
    tertiary = Success,
    background = BgCream,
    surface = CardBg,
    onPrimary = Color.White,
    onSecondary = TextPrimary,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = Danger,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBrown,
    secondary = AccentLight,
    tertiary = Success,
    background = BgCream,
    surface = CardBg,
    onPrimary = Color.White,
    onSecondary = TextPrimary,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = Danger,
    onError = Color.White
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}