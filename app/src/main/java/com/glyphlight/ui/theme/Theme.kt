package com.glyphlight.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val OledBlack = Color(0xFF000000)
val SurfaceDark = Color(0xFF0B0B0C)
val CardDark = Color(0xFF1B1D1F)
val DotDim = Color(0xFF3A3A3C)
val TextDim = Color(0xFF9A9A9E)

@Composable
fun GlyphlightTheme(accentColor: Color, content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = accentColor,
        onPrimary = Color.White,
        secondary = accentColor,
        onSecondary = Color.White,
        tertiary = accentColor,
        background = OledBlack,
        onBackground = Color.White,
        surface = SurfaceDark,
        onSurface = Color.White,
        surfaceVariant = CardDark,
        onSurfaceVariant = TextDim,
        error = accentColor,
    )
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
