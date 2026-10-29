package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    secondary = NeonPink,
    onSecondary = Color.White,
    tertiary = NeonYellow,
    background = DarkNavyBg,
    onBackground = TextPrimary,
    surface = DarkNavyCard,
    onSurface = TextPrimary,
    surfaceVariant = DarkNavySurface,
    onSurfaceVariant = TextSecondary
)

@Composable
fun BrickSmashTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
