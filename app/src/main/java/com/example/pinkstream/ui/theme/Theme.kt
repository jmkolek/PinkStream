package com.example.pinkstream.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * The app theme: Material 3 colours, type and shapes for stock components, plus
 * [DeckColors] for the colours Material has no slot for.
 * Light only, whatever the phone's dark theme says. No dynamic colour: the pink palette is the point.
 */
@Composable
fun PinkStreamTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDeckColors provides DayDeckColors) {
        MaterialTheme(
            colorScheme = DayColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}

/** Shortcut for screens: `DeckTheme.colors.primary`. Type and shapes are `DeckType` and `DeckShapes`. */
object DeckTheme {
    val colors: DeckColors
        @Composable @ReadOnlyComposable
        get() = LocalDeckColors.current
}
