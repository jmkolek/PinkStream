package com.example.pinkstream.ui.settings

import com.example.pinkstream.data.streamerbot.ConnectionState

/** What we know about the action list on the connected Streamer.bot. */
sealed interface ActionsLookup {
    data object Unknown : ActionsLookup
    data object Loading : ActionsLookup
    data class Found(val count: Int) : ActionsLookup
    data object Failed : ActionsLookup
}

/** Decides the icon tile colour of the status row. */
enum class StatusKind { CONNECTED, WORKING, PROBLEM, IDLE }

/** The status row at the top of the Connection card. */
data class ConnectionStatus(val kind: StatusKind, val title: String, val subtitle: String)

/**
 * Turns the client state into the status row's words.
 * [lastFailure] is the reason the previous attempt failed. While the client quietly
 * retries, we keep showing that reason instead of flicking back to "Connecting…".
 */
fun connectionStatus(state: ConnectionState, lookup: ActionsLookup, lastFailure: String?): ConnectionStatus =
    when (state) {
        is ConnectionState.Connected -> ConnectionStatus(
            StatusKind.CONNECTED,
            "Connected",
            when (lookup) {
                is ActionsLookup.Found -> "Streamer.bot · ${lookup.count} ${if (lookup.count == 1) "action" else "actions"} found"
                ActionsLookup.Failed -> "Streamer.bot · Couldn't list the actions"
                ActionsLookup.Loading, ActionsLookup.Unknown -> "Streamer.bot · Looking for actions…"
            },
        )
        is ConnectionState.Failed ->
            if (state.passwordNeeded) {
                ConnectionStatus(StatusKind.PROBLEM, "Password needed", "Streamer.bot asks for one. Enter it below.")
            } else {
                ConnectionStatus(StatusKind.PROBLEM, "Can't connect", state.reason)
            }
        ConnectionState.Connecting, ConnectionState.Authenticating ->
            if (lastFailure != null) {
                ConnectionStatus(StatusKind.PROBLEM, "Can't connect", "$lastFailure Trying again…")
            } else {
                ConnectionStatus(
                    StatusKind.WORKING,
                    "Connecting…",
                    if (state == ConnectionState.Authenticating) "Signing in to Streamer.bot" else "Looking for Streamer.bot on your PC",
                )
            }
        ConnectionState.Disconnected ->
            ConnectionStatus(StatusKind.IDLE, "Not connected", "Tap Test connection to connect.")
    }
