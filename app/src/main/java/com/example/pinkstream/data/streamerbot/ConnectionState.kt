package com.example.pinkstream.data.streamerbot

/** Where the connection to Streamer.bot is at. */
sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data object Authenticating : ConnectionState
    data class Connected(val info: ServerInfo?) : ConnectionState
    /** [reason] is written for the user, e.g. "Password needed". */
    data class Failed(val reason: String) : ConnectionState
}
