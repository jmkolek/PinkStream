package com.example.pinkstream.data.deck

import kotlinx.serialization.Serializable

// The layout as saved before the key art redesign: pages of sections (a mic "hero"
// or a grid of tiles), each button with a line icon and a tile colour. Only read, to
// move old layouts and old backup files over (LayoutMigration). Icon, colour and
// style are plain strings so the old enums could be deleted.

@Serializable
internal data class LegacyLayout(val pages: List<LegacyPage>)

@Serializable
internal data class LegacyPage(val id: String, val name: String, val sections: List<LegacySection>)

@Serializable
internal data class LegacySection(
    val id: String,
    val title: String? = null,
    val style: String,                // "HERO" or "GRID"
    val buttons: List<LegacyButton>,
)

@Serializable
internal data class LegacyButton(
    val id: String,
    val label: String,
    val icon: String,                 // DeckIcon name, e.g. "GAMEPAD"
    val color: String,                // TileColor name, e.g. "ROSE"
    val actionId: String? = null,
    val actionName: String? = null,
    val onAirScene: String? = null,
    val stateInput: String? = null,
    val confirm: Boolean = false,
)
