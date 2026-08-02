package com.aboma.tvlauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF27E0C3),
    secondary = Color(0xFFFFC857),
    background = Color(0xFF05070A),
    surface = Color(0xFF111820),
    surfaceVariant = Color(0xFF1B2633),
    onPrimary = Color(0xFF04110F),
    onSecondary = Color(0xFF171004),
    onBackground = Color(0xFFF6F7FB),
    onSurface = Color(0xFFF6F7FB),
    onSurfaceVariant = Color(0xFFC7D0DA),
)

@Composable
fun TVLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}
