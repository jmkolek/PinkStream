package com.example.pinkstream.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Background and icon colour for one [IconTint]. */
@Immutable
data class IconBoxPalette(val container: Color, val icon: Color)

/**
 * The design tokens from DESIGN.md that Material 3's ColorScheme has no slot for
 * (key shadows, icon boxes, the connection dot…). Screens read them with `DeckTheme.colors`.
 */
@Immutable
data class DeckColors(
    val background: Color,
    val surface: Color,
    val hairline: Color,
    val ink: Color,
    val inkSecondary: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val boxRose: IconBoxPalette,
    val boxPeach: IconBoxPalette,
    val boxLilac: IconBoxPalette,
    val boxMist: IconBoxPalette,
    val connected: Color,
    val connectedHalo: Color,
    val offline: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val outline: Color,
    val fieldBg: Color,
    val fieldBorder: Color,
    val placeholder: Color,
    val highlight: Color,
    val highlightSelected: Color,
    val sheetHandle: Color,
    val scrim: Color,
    val error: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val raisedShadow: Color,      // the coloured part of the raised-card shadow
    val segmentShadow: Color,     // under the selected option of a segmented control
    // Key shadows (README.md → Shadows): a hard 3 dp edge plus a soft glow, per state.
    val keyEdgeOff: Color,
    val keyGlowOff: Color,
    val keyEdgeOn: Color,
    val keyGlowOn: Color,
    val keyLifted: Color,         // under a key being dragged in edit layout mode
    val pageDot: Color,           // the inactive page dots above the dock
) {
    fun iconBox(tint: IconTint): IconBoxPalette = when (tint) {
        IconTint.ROSE -> boxRose
        IconTint.PEACH -> boxPeach
        IconTint.LILAC -> boxLilac
        IconTint.MIST -> boxMist
    }
}

/**
 * A CompositionLocal passes a value down the UI tree without handing it to every
 * composable as a parameter. PinkStreamTheme provides the (only) light set.
 */
val LocalDeckColors = staticCompositionLocalOf { DayDeckColors }
