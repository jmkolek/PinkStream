package com.example.pinkstream.ui.icons

import androidx.annotation.DrawableRes
import com.example.pinkstream.R

/**
 * The icons the action picker guesses for its rows from an action's name (mockup 04).
 * Keys use [DeckKeyArt] and dock tabs use [PageIcon]; interface icons (chevrons,
 * pencil, sliders…) are used straight from R.drawable.
 */
enum class DeckIcon(@get:DrawableRes val drawable: Int) {
    MONITOR(R.drawable.ic_deck_monitor),
    VOLUME(R.drawable.ic_deck_volume),
    SMILE(R.drawable.ic_deck_smile),
    BOLT(R.drawable.ic_deck_bolt),
}
