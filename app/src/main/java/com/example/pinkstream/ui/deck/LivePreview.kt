package com.example.pinkstream.ui.deck

import com.example.pinkstream.data.live.LiveState

/**
 * Debug builds only: fake OBS states that reproduce the two deck mockups, so the tile
 * looks can be checked before milestone 5. Names match the default layout.
 */
enum class LivePreview(val label: String, val state: LiveState?) {
    REAL("Real state", null),
    DAY_MOCKUP(
        "Day mockup: Gaming on air, mic live, desktop on, music off",
        LiveState(currentScene = "Gaming", muted = mapOf("Mic/Aux" to false, "Desktop Audio" to false, "Music" to true)),
    ),
    NIGHT_MOCKUP(
        "Night mockup: BRB on air, mic muted, desktop off, music on",
        LiveState(currentScene = "BRB", muted = mapOf("Mic/Aux" to true, "Desktop Audio" to true, "Music" to false)),
    ),
}
