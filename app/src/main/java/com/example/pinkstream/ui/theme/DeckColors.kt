package com.example.pinkstream.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Background, icon and label colour for one tile colour. */
@Immutable
data class TilePalette(val container: Color, val icon: Color, val label: Color)

/** Every colour of the mic hero in one state (Live or Muted). */
@Immutable
data class HeroPalette(
    val container: Color,
    val border: Color,            // Color.Transparent = no border
    val shadow: Color,            // Color.Transparent = no shadow
    val title: Color,
    val subtitle: Color,
    val iconBox: Color,
    val icon: Color,
    val pill: Color,
    val pillText: Color,
)

/**
 * The design tokens from DESIGN.md that Material 3's ColorScheme has no slot for
 * (tiles, pills, the hero, connection dot…). Screens read them with `DeckTheme.colors`.
 */
@Immutable
data class DeckColors(
    val background: Color,
    val surface: Color,
    val hairline: Color,
    val ink: Color,
    val inkSecondary: Color,
    val inactiveTab: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val tileRose: TilePalette,
    val tilePeach: TilePalette,
    val tileLilac: TilePalette,
    val tileMist: TilePalette,
    val tileBerry: TilePalette,
    val tilePlum: TilePalette,
    val toggleOff: Color,
    val toggleOffBorder: Color,
    val toggleOffIcon: Color,
    val pillOn: Color,            // its text uses the tile's icon colour
    val pillOff: Color,
    val pillOffText: Color,
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
    val onAirShadow: Color,
    val raisedShadow: Color,      // the coloured part of the raised-card shadow (day only)
    val heroLive: HeroPalette,
    val heroMuted: HeroPalette,
) {
    fun tile(color: TileColor): TilePalette = when (color) {
        TileColor.ROSE -> tileRose
        TileColor.PEACH -> tilePeach
        TileColor.LILAC -> tileLilac
        TileColor.MIST -> tileMist
        TileColor.BERRY -> tileBerry
        TileColor.PLUM -> tilePlum
    }
}

/**
 * A CompositionLocal passes a value down the UI tree without handing it to every
 * composable as a parameter. PinkStreamTheme provides the day or night set.
 */
val LocalDeckColors = staticCompositionLocalOf { DayDeckColors }
