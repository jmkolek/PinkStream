package com.example.pinkstream.ui.deck

import com.example.pinkstream.data.deck.DeckButton
import com.example.pinkstream.data.live.LiveState
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.ui.components.ChipStatus
import com.example.pinkstream.ui.components.MicState
import com.example.pinkstream.ui.components.TileLook

/** How a GRID tile looks, from its settings and what OBS is doing. "No action" wins: it needs fixing. */
fun DeckButton.tileLook(live: LiveState, hasAction: Boolean): TileLook {
    val muted = stateInput?.let { live.muted[it] }
    return when {
        !hasAction -> TileLook.NO_ACTION
        onAirScene != null && onAirScene == live.currentScene -> TileLook.ON_AIR
        muted == false -> TileLook.TOGGLE_ON
        muted == true -> TileLook.TOGGLE_OFF
        else -> TileLook.NORMAL
    }
}

/** The hero's Live / Muted pill, or UNKNOWN (no pill) when we don't know the input's state. */
fun DeckButton.micState(live: LiveState): MicState = when (stateInput?.let { live.muted[it] }) {
    true -> MicState.MUTED
    false -> MicState.LIVE
    null -> MicState.UNKNOWN
}

/**
 * The connection chip. After a failure the client keeps retrying, briefly going
 * through Connecting each time; [failedSinceConnected] keeps the chip on "Offline"
 * through those retries instead of flickering.
 */
fun chipStatus(state: ConnectionState, failedSinceConnected: Boolean): ChipStatus = when (state) {
    is ConnectionState.Connected -> ChipStatus.CONNECTED
    ConnectionState.Connecting, ConnectionState.Authenticating ->
        if (failedSinceConnected) ChipStatus.OFFLINE else ChipStatus.CONNECTING
    ConnectionState.Disconnected, is ConnectionState.Failed -> ChipStatus.OFFLINE
}

/** Updates the "failed since the last good connection" flag that [chipStatus] needs. */
fun failedSinceConnected(state: ConnectionState, before: Boolean): Boolean = when (state) {
    is ConnectionState.Connected, ConnectionState.Disconnected -> false
    is ConnectionState.Failed -> true
    ConnectionState.Connecting, ConnectionState.Authenticating -> before
}

/** The state words TalkBack adds after a tile's label; null when there's nothing to add. */
fun TileLook.spoken(): String? = when (this) {
    TileLook.ON_AIR -> "on air"
    TileLook.TOGGLE_ON -> "on"
    TileLook.TOGGLE_OFF -> "off"
    TileLook.NO_ACTION -> "no action"
    TileLook.NORMAL -> null
}

fun MicState.spoken(): String? = when (this) {
    MicState.LIVE -> "live"
    MicState.MUTED -> "muted"
    MicState.UNKNOWN -> null
}

/** What TalkBack says for a tile: its label, then its state, e.g. "Microphone, live". */
fun spokenTile(label: String, vararg states: String?, offline: Boolean = false): String =
    (listOf(label) + states.filterNotNull() + listOfNotNull("not connected".takeIf { offline })).joinToString(", ")
