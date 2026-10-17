package com.example.pinkstream.data.live

import com.example.pinkstream.data.streamerbot.EventEnvelope
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Turns the OBS events Streamer.bot passes on into [LiveState] changes. This is the
 * fallback, used while the direct OBS connection (ObsClient) is down: it knows the
 * scene, never the mute states.
 *
 * What real traffic showed (Streamer.bot 1.0.7, OBS 32.2.2, WebSocket 5.7.4, Oct 2026):
 * - Obs.SceneChanged: `data.scene.sceneName` is the new program scene. `data.oldScene`
 *   has the same shape, `data.connection` describes the OBS connection, and both scenes
 *   carry a long `sceneItems` list we ignore.
 * - Obs.Event: never arrived. Six scene switches and muting/unmuting Mic/Aux in the OBS
 *   mixer sent nothing, so raw OBS events such as InputMuteStateChanged don't reach
 *   WebSocket clients, and mute state can't be read from Streamer.bot events.
 * - Nothing tells us the scene or mute state when we connect; it's unknown until the
 *   first change. That's why the app also talks to OBS directly (ObsLiveEvents).
 */
object StreamerBotEvents {

    /** What the app subscribes to after every connect. */
    val SUBSCRIPTION: Map<String, List<String>> =
        mapOf("Obs" to listOf("SceneChanged", "Event", "StreamingStarted", "StreamingStopped", "Disconnected"))

    fun isSceneChange(event: EventEnvelope): Boolean =
        event.event.source == "Obs" && event.event.type == "SceneChanged"

    /** The state after [event]. Events we don't understand leave it as it was. */
    fun apply(state: LiveState, event: EventEnvelope): LiveState {
        if (event.event.source != "Obs") return state
        return when (event.event.type) {
            "SceneChanged" -> sceneName(event.data)?.let { state.copy(currentScene = it) } ?: state
            // Streamer.bot lost OBS: what we knew may no longer be true.
            "Disconnected" -> LiveState()
            else -> state
        }
    }

    private fun sceneName(data: JsonObject?): String? {
        val scene = data?.get("scene") as? JsonObject ?: return null
        return (scene["sceneName"] as? JsonPrimitive)?.takeIf { it.isString }?.content
    }
}
