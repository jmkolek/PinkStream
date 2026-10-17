package com.example.pinkstream.ui.deck

import com.example.pinkstream.data.deck.DeckButton
import com.example.pinkstream.data.live.LiveState
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.ui.components.ChipStatus
import com.example.pinkstream.ui.components.MicState
import com.example.pinkstream.ui.components.TileLook
import com.example.pinkstream.ui.icons.DeckIcon
import com.example.pinkstream.ui.theme.TileColor
import org.junit.Assert.assertEquals
import org.junit.Test

class TileStateTest {

    private val scene = DeckButton("1", "Gaming", DeckIcon.GAMEPAD, TileColor.ROSE, actionName = "a", onAirScene = "Gaming")
    private val toggle = DeckButton("2", "Music", DeckIcon.MUSIC, TileColor.PEACH, actionName = "a", stateInput = "Music")
    private val mic = DeckButton("3", "Microphone", DeckIcon.MIC, TileColor.ROSE, actionName = "a", stateInput = "Mic/Aux")

    @Test
    fun sceneTileIsOnAirOnlyForItsScene() {
        assertEquals(TileLook.ON_AIR, scene.tileLook(LiveState(currentScene = "Gaming"), hasAction = true))
        assertEquals(TileLook.NORMAL, scene.tileLook(LiveState(currentScene = "BRB"), hasAction = true))
        assertEquals(TileLook.NORMAL, scene.tileLook(LiveState(), hasAction = true))
    }

    @Test
    fun toggleShowsOnOffOnlyWhenKnown() {
        assertEquals(TileLook.TOGGLE_ON, toggle.tileLook(LiveState(muted = mapOf("Music" to false)), hasAction = true))
        assertEquals(TileLook.TOGGLE_OFF, toggle.tileLook(LiveState(muted = mapOf("Music" to true)), hasAction = true))
        assertEquals(TileLook.NORMAL, toggle.tileLook(LiveState(muted = mapOf("Other" to true)), hasAction = true))
    }

    @Test
    fun noActionWins() {
        assertEquals(TileLook.NO_ACTION, scene.tileLook(LiveState(currentScene = "Gaming"), hasAction = false))
    }

    @Test
    fun micStateFollowsItsInput() {
        assertEquals(MicState.MUTED, mic.micState(LiveState(muted = mapOf("Mic/Aux" to true))))
        assertEquals(MicState.LIVE, mic.micState(LiveState(muted = mapOf("Mic/Aux" to false))))
        assertEquals(MicState.UNKNOWN, mic.micState(LiveState()))
    }

    @Test
    fun chipStaysOfflineWhileRetryingAfterAFailure() {
        var failed = false
        fun step(state: ConnectionState): ChipStatus {
            failed = failedSinceConnected(state, failed)
            return chipStatus(state, failed)
        }
        assertEquals(ChipStatus.CONNECTING, step(ConnectionState.Connecting))         // first attempt
        assertEquals(ChipStatus.CONNECTED, step(ConnectionState.Connected(null)))
        assertEquals(ChipStatus.OFFLINE, step(ConnectionState.Failed("Lost the connection to your PC.")))
        assertEquals(ChipStatus.OFFLINE, step(ConnectionState.Connecting))            // retry: no flicker
        assertEquals(ChipStatus.CONNECTED, step(ConnectionState.Connected(null)))
        assertEquals(ChipStatus.OFFLINE, step(ConnectionState.Disconnected))          // app in background
        assertEquals(ChipStatus.CONNECTING, step(ConnectionState.Connecting))         // back in front
    }

    @Test
    fun talkBackHearsLabelThenState() {
        assertEquals("Microphone, live", spokenTile("Microphone", MicState.LIVE.spoken()))
        assertEquals("Microphone, muted", spokenTile("Microphone", MicState.MUTED.spoken()))
        assertEquals("Microphone", spokenTile("Microphone", MicState.UNKNOWN.spoken()))
        assertEquals("Gaming, on air", spokenTile("Gaming", TileLook.ON_AIR.spoken()))
        assertEquals("Music, off, not connected", spokenTile("Music", TileLook.TOGGLE_OFF.spoken(), offline = true))
        assertEquals("Microphone, no action", spokenTile("Microphone", MicState.UNKNOWN.spoken(), "no action"))
    }
}
