package com.example.pinkstream.data.deck

import com.example.pinkstream.ui.icons.DeckIcon
import com.example.pinkstream.ui.theme.TileColor
import kotlinx.serialization.Serializable

// The deck layout. Saved as one JSON string, so every class here is @Serializable
// (enums like DeckIcon and TileColor are saved by name without needing it).

@Serializable
data class DeckLayout(val pages: List<DeckPage>)

@Serializable
data class DeckPage(val id: String, val name: String, val sections: List<DeckSection>)

@Serializable
data class DeckSection(
    val id: String,
    val title: String? = null,
    val style: SectionStyle,
    val buttons: List<DeckButton>,
)

/** HERO = one full-width button (the mic), GRID = tiles. */
@Serializable
enum class SectionStyle { HERO, GRID }

@Serializable
data class DeckButton(
    val id: String,
    val label: String,
    val icon: DeckIcon,
    val color: TileColor,
    val actionId: String? = null,     // Streamer.bot action GUID (preferred)
    val actionName: String? = null,   // shown in the editor; used to find the id again
    val onAirScene: String? = null,   // OBS scene name: the tile lights up while it's live (milestone 5)
    val stateInput: String? = null,   // OBS input name: the tile shows On/Off, or Live/Muted on the hero (milestone 5)
    val confirm: Boolean = false,     // ask before running
)
