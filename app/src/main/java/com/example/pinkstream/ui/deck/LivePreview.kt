package com.example.pinkstream.ui.deck

import com.example.pinkstream.data.live.LiveState

/**
 * Debug builds only: fake OBS states (the first matches the main mockup), so the key
 * looks can be checked without OBS. A key lights up only if its OBS scene or input name
 * matches the fake one ("Gaming", "BRB", "Mic/Aux", "Desktop Audio", "Music").
 */
enum class LivePreview(val label: String, val state: LiveState?) {
    REAL("Real state", null),
    DAY_MOCKUP(
        "Main mockup: Gaming on air, mic live, desktop on, music off",
        LiveState(currentScene = "Gaming", muted = mapOf("Mic/Aux" to false, "Desktop Audio" to false, "Music" to true)),
    ),
    SECOND_STATE(
        "Other state: BRB on air, mic muted, desktop off, music on",
        LiveState(currentScene = "BRB", muted = mapOf("Mic/Aux" to true, "Desktop Audio" to true, "Music" to false)),
    ),
}
