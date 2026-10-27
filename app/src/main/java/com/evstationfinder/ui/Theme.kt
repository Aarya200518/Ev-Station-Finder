package com.evstationfinder.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF0F9F68)
private val DeepGreen = Color(0xFF08764B)
private val Mint = Color(0xFFD7F1E2)
private val Pale = Color(0xFFF4F8F5)
private val Ink = Color(0xFF12221D)
private val Muted = Color(0xFF708077)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = DeepGreen,
    secondary = Color(0xFF28513F),
    background = Pale,
    surface = Color.White,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = Muted,
    outline = Color(0xFFD9E5DC)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF65D69D),
    onPrimary = Color(0xFF00391F),
    primaryContainer = Color(0xFF075D3A),
    onPrimaryContainer = Color(0xFFB1F3C9),
    background = Color(0xFF0D1712),
    surface = Color(0xFF16231B),
    onBackground = Color(0xFFE7F1EA),
    onSurface = Color(0xFFE7F1EA),
    onSurfaceVariant = Color(0xFFAABAB0),
    outline = Color(0xFF3B5043)
)

@Composable
fun EVStationFinderTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors, content = content)
}
