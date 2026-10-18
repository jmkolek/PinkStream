package com.example.pinkstream.data.deck

import com.example.pinkstream.ui.icons.DeckKeyArt
import com.example.pinkstream.ui.icons.PageIcon
import kotlinx.serialization.Serializable

// The deck layout. Saved as one JSON string, so every class here is @Serializable
// (enums like DeckKeyArt and PageIcon are saved by name without needing it).
// Layouts saved before the key art redesign have a different shape; LayoutMigration
// reads those (LegacyLayout.kt).

@Serializable
data class DeckLayout(val pages: List<DeckPage>)

/** One page of keys, shown as a tab in the bottom dock. Keys fill the grid in order. */
@Serializable
data class DeckPage(val id: String, val name: String, val icon: PageIcon, val keys: List<DeckKey>)

@Serializable
data class DeckKey(
    val id: String,
    val label: String,                // not drawn on the key; TalkBack, the chips and the picker use it
    val art: DeckKeyArt,
    val actionId: String? = null,     // Streamer.bot action GUID (preferred)
    val actionName: String? = null,   // shown in the editor; used to find the id again
    val onAirScene: String? = null,   // OBS scene name: the key shows its on art while it's live
    val stateInput: String? = null,   // OBS input name: on art while unmuted, off art while muted
    val confirm: Boolean = false,     // ask before running
)
