package com.example.pinkstream.data.obs

/** Where the direct, read-only connection to OBS is at. */
sealed interface ObsConnectionState {
    data object Disconnected : ObsConnectionState
    data object Connecting : ObsConnectionState
    data object Connected : ObsConnectionState
    /**
     * [reason] is written for the user. [passwordNeeded]: OBS asks for a password and
     * none is saved. The client keeps this state while it quietly retries.
     */
    data class Failed(val reason: String, val passwordNeeded: Boolean = false) : ObsConnectionState
}

