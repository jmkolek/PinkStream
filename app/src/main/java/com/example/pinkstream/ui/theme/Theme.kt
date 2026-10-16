package com.example.pinkstream.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * The app theme: Material 3 colours, type and shapes for stock components, plus
 * [DeckColors] for the tile and state colours Material has no slot for.
 * No dynamic colour: the pink palette is the point.
 */
@Composable
fun PinkStreamTheme(
    themeMode: ThemeMode = ThemeMode.AUTO,
    content: @Composable () -> Unit,
) {
    val night = themeMode.isNight(isSystemInDarkTheme())
    CompositionLocalProvider(LocalDeckColors provides if (night) NightDeckColors else DayDeckColors) {
        MaterialTheme(
            colorScheme = if (night) NightColorScheme else DayColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}

/** Shortcut for screens: `DeckTheme.colors.tileRose`. Type and shapes are `DeckType` and `DeckShapes`. */
object DeckTheme {
    val colors: DeckColors
        @Composable @ReadOnlyComposable
        get() = LocalDeckColors.current
}
