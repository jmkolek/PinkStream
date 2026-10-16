package com.example.pinkstream.ui.settings

import com.example.pinkstream.data.streamerbot.ConnectionState
import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionStatusTest {

    private val connected = ConnectionState.Connected(info = null)

    @Test
    fun connectedShowsTheActionCount() {
        assertEquals(
            ConnectionStatus(StatusKind.CONNECTED, "Connected", "Streamer.bot · 14 actions found"),
            connectionStatus(connected, ActionsLookup.Found(14), lastFailure = null),
        )
        assertEquals("Streamer.bot · 1 action found", connectionStatus(connected, ActionsLookup.Found(1), null).subtitle)
        assertEquals("Streamer.bot · Looking for actions…", connectionStatus(connected, ActionsLookup.Loading, null).subtitle)
    }

    @Test
    fun failureShowsTheReason() {
        val status = connectionStatus(ConnectionState.Failed("Can't reach your PC. Is Streamer.bot running?"), ActionsLookup.Unknown, null)
        assertEquals(ConnectionStatus(StatusKind.PROBLEM, "Can't connect", "Can't reach your PC. Is Streamer.bot running?"), status)
    }

    @Test
    fun passwordNeededHasItsOwnTitle() {
        val status = connectionStatus(ConnectionState.Failed("Password needed", passwordNeeded = true), ActionsLookup.Unknown, null)
        assertEquals("Password needed", status.title)
    }

    @Test
    fun retryingAfterAFailureKeepsTheReason() {
        val status = connectionStatus(ConnectionState.Connecting, ActionsLookup.Unknown, lastFailure = "Can't reach your PC.")
        assertEquals(ConnectionStatus(StatusKind.PROBLEM, "Can't connect", "Can't reach your PC. Trying again…"), status)
    }

    @Test
    fun firstAttemptShowsConnecting() {
        assertEquals("Connecting…", connectionStatus(ConnectionState.Connecting, ActionsLookup.Unknown, null).title)
        assertEquals(StatusKind.WORKING, connectionStatus(ConnectionState.Authenticating, ActionsLookup.Unknown, null).kind)
    }
}
