package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PinkPrimary,
    onPrimary = Color.White,
    secondary = PinkLight,
    onSecondary = Color.Black,
    tertiary = PinkAccent,
    background = DarkBg,
    onBackground = Color(0xFFFCEFF1),
    surface = DarkSurface,
    onSurface = Color(0xFFFCEFF1),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFE8D5D8)
)

private val LightColorScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = Color.White,
    secondary = PinkLight,
    onSecondary = Color.Black,
    tertiary = PinkAccent,
    background = LightBg,
    onBackground = Color(0xFF2C191E),
    surface = LightSurface,
    onSurface = Color(0xFF2C191E),
    surfaceVariant = PinkVeryLight,
    onSurfaceVariant = Color(0xFF5A4449)
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
