package com.example.pinkstream.data.live

/**
 * What OBS is doing right now, as far as we know.
 * [currentScene] null = unknown. [muted] maps an OBS input name to its mute state;
 * an input that isn't in the map is unknown (the tile shows no pill).
 */
data class LiveState(
    val currentScene: String? = null,
    val muted: Map<String, Boolean> = emptyMap(),
)
