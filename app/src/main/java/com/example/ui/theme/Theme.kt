package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = FutaPrimary,
    onPrimary = Color.White,
    primaryContainer = FutaPrimaryLight,
    onPrimaryContainer = FutaPrimaryVariant,
    secondary = FutaCyan,
    onSecondary = Color.White,
    secondaryContainer = FutaCyanContainer,
    onSecondaryContainer = Color(0xFF00384D),
    tertiary = FutaGold,
    onTertiary = Color.White,
    tertiaryContainer = FutaGoldContainer,
    onTertiaryContainer = Color(0xFF5A3600),
    background = FutaBackground,
    onBackground = FutaTextPrimary,
    surface = FutaSurface,
    onSurface = FutaTextPrimary,
    surfaceVariant = FutaSurfaceVariant,
    onSurfaceVariant = FutaTextSecondary,
    outline = FutaBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4D97FF),
    onPrimary = Color(0xFF00296B),
    primaryContainer = Color(0xFF003E9A),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF4DE1FF),
    onSecondary = Color(0xFF003644),
    secondaryContainer = Color(0xFF004D60),
    onSecondaryContainer = Color(0xFFBAEDFF),
    tertiary = Color(0xFFFFB84D),
    onTertiary = Color(0xFF4A2800),
    background = Color(0xFF0B132B),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF111E38),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1C2C4E),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF2E406B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Enforce our branded Wave-like FinTech palette
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
