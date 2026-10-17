package com.example.pinkstream.data.live

import com.example.pinkstream.data.obs.ObsEvent
import com.example.pinkstream.data.obs.ObsMessages.string
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

/**
 * Turns events from the direct OBS connection into [LiveState] changes. The names are
 * from the OBS WebSocket 5 protocol docs; checked against OBS 32.2.2 (WebSocket 5.7.4).
 */
object ObsLiveEvents {

    fun isSceneChange(event: ObsEvent): Boolean = event.type == "CurrentProgramSceneChanged"

    /** The state after [event]. Events we don't use leave it as it was. */
    fun apply(state: LiveState, event: ObsEvent): LiveState {
        val data = event.data
        return when (event.type) {
            "CurrentProgramSceneChanged" ->
                data.string("sceneName")?.let { state.copy(currentScene = it) } ?: state
            "InputMuteStateChanged" -> {
                val input = data.string("inputName")
                val muted = (data["inputMuted"] as? JsonPrimitive)?.booleanOrNull
                if (input != null && muted != null) state.copy(muted = state.muted + (input to muted)) else state
            }
            // Keep a renamed input's state under its new name.
            "InputNameChanged" -> {
                val old = data.string("oldInputName")
                val new = data.string("inputName")
                val muted = old?.let { state.muted[it] }
                if (old != null && new != null && muted != null) {
                    state.copy(muted = state.muted - old + (new to muted))
                } else {
                    state
                }
            }
            "InputRemoved" -> data.string("inputName")?.let { state.copy(muted = state.muted - it) } ?: state
            else -> state
        }
    }
}
