package com.example.pinkstream.ui.settings

import com.example.pinkstream.data.obs.ObsConnectionState

/**
 * The OBS status row's words. The OBS client stays on Failed while it quietly
 * retries, so there's no "last failure" to remember here.
 */
fun obsStatus(state: ObsConnectionState): ConnectionStatus = when (state) {
    ObsConnectionState.Connected ->
        ConnectionStatus(StatusKind.CONNECTED, "OBS connected", "Scenes and mute states come straight from OBS")
    ObsConnectionState.Connecting ->
        ConnectionStatus(StatusKind.WORKING, "Connecting to OBS…", "Looking for OBS's WebSocket server")
    is ObsConnectionState.Failed ->
        if (state.passwordNeeded) {
            ConnectionStatus(StatusKind.PROBLEM, "OBS password needed", "OBS asks for one. Enter it below.")
        } else {
            ConnectionStatus(StatusKind.PROBLEM, "Can't connect to OBS", state.reason)
        }
    ObsConnectionState.Disconnected ->
        ConnectionStatus(StatusKind.IDLE, "OBS not connected", "Tap Test connection to connect.")
}
