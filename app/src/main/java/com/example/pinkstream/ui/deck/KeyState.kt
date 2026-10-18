package com.example.pinkstream.ui.deck

import com.example.pinkstream.data.deck.DeckKey
import com.example.pinkstream.data.deck.DeckPage
import com.example.pinkstream.data.live.LiveState
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.ui.components.ChipStatus
import com.example.pinkstream.ui.icons.DeckKeyArt

/** What OBS says about a key: null = unknown, or the key doesn't follow anything. */
enum class KeyState { ON_AIR, ON, OFF }

fun DeckKey.state(live: LiveState): KeyState? {
    if (onAirScene != null && onAirScene == live.currentScene) return KeyState.ON_AIR
    return when (stateInput?.let { live.muted[it] }) {
        false -> KeyState.ON
        true -> KeyState.OFF
        null -> null
    }
}

/** On art for a live scene or an unmuted input; off art for everything else. */
fun KeyState?.isOn(): Boolean = this == KeyState.ON_AIR || this == KeyState.ON

/** The state words TalkBack adds after a key's name. The mic says live / muted. */
fun KeyState?.spoken(art: DeckKeyArt): String? = when (this) {
    KeyState.ON_AIR -> "on air"
    KeyState.ON -> if (art == DeckKeyArt.MIC) "live" else "on"
    KeyState.OFF -> if (art == DeckKeyArt.MIC) "muted" else "off"
    null -> null
}

/** What TalkBack says for a key: its name, then its state, e.g. "Microphone, live". */
fun spokenKey(label: String, vararg states: String?, offline: Boolean = false): String =
    (listOf(label) + states.filterNotNull() + listOfNotNull("not connected".takeIf { offline })).joinToString(", ")

/**
 * The "On air · Gaming" chip: the name of the key whose scene is live, else the OBS
 * scene name itself. null (no chip) while the scene is unknown.
 */
fun onAirChipText(pages: List<DeckPage>, live: LiveState): String? {
    val scene = live.currentScene ?: return null
    return pages.asSequence().flatMap { it.keys }.firstOrNull { it.onAirScene == scene }?.label ?: scene
}

/**
 * The "Mic live" / "Mic muted" chip follows the first key with the Microphone art that
 * has an OBS input. true = live, false = muted, null = no chip (no such key, or unknown).
 */
fun micChipLive(pages: List<DeckPage>, live: LiveState): Boolean? {
    val mic = pages.asSequence().flatMap { it.keys }.firstOrNull { it.art == DeckKeyArt.MIC && it.stateInput != null }
        ?: return null
    return live.muted[mic.stateInput]?.not()
}

/**
 * How many empty slots edit layout mode adds after a page's keys: enough to fill the
 * usual grid (3 × 5 or 4 × 6), and always at least one so there's somewhere to tap.
 */
fun emptySlotCount(keyCount: Int, columns: Int): Int {
    val rows = if (columns <= 3) 5 else 6
    val full = columns * rows
    if (keyCount < full) return full - keyCount
    // A fuller page: finish the last row, or add a whole new one if it's complete.
    val rest = keyCount % columns
    return if (rest == 0) columns else columns - rest
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
