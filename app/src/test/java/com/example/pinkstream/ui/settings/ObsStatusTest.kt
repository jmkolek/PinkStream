package com.example.pinkstream.ui.settings

import com.example.pinkstream.data.obs.ObsConnectionState
import org.junit.Assert.assertEquals
import org.junit.Test

class ObsStatusTest {

    @Test
    fun connectedSaysWhereTheStateComesFrom() {
        val status = obsStatus(ObsConnectionState.Connected)
        assertEquals(StatusKind.CONNECTED, status.kind)
        assertEquals("OBS connected", status.title)
    }

    @Test
    fun failureShowsTheReason() {
        val status = obsStatus(ObsConnectionState.Failed("Can't reach OBS. Is it running, with its WebSocket server on?"))
        assertEquals(ConnectionStatus(StatusKind.PROBLEM, "Can't connect to OBS", "Can't reach OBS. Is it running, with its WebSocket server on?"), status)
    }

    @Test
    fun passwordNeededHasItsOwnTitle() {
        val status = obsStatus(ObsConnectionState.Failed("OBS password needed", passwordNeeded = true))
        assertEquals("OBS password needed", status.title)
        assertEquals(StatusKind.PROBLEM, status.kind)
    }

    @Test
    fun connectingAndDisconnected() {
        assertEquals(StatusKind.WORKING, obsStatus(ObsConnectionState.Connecting).kind)
        assertEquals(StatusKind.IDLE, obsStatus(ObsConnectionState.Disconnected).kind)
    }
}
