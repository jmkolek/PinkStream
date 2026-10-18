package com.example.pinkstream.ui.deck

import com.example.pinkstream.data.deck.DeckKey
import com.example.pinkstream.data.deck.DeckPage
import com.example.pinkstream.data.live.LiveState
import com.example.pinkstream.data.streamerbot.ConnectionState
import com.example.pinkstream.ui.components.ChipStatus
import com.example.pinkstream.ui.icons.DeckKeyArt
import com.example.pinkstream.ui.icons.PageIcon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyStateTest {

    private val scene = DeckKey("1", "Gaming", DeckKeyArt.GAMING, actionName = "a", onAirScene = "InGame Scene")
    private val music = DeckKey("2", "Music", DeckKeyArt.MUSIC, actionName = "a", stateInput = "Music")
    private val mic = DeckKey("3", "Microphone", DeckKeyArt.MIC, actionName = "a", stateInput = "Mic/Aux")
    private val pages = listOf(DeckPage("p", "Main", PageIcon.GRID, listOf(scene, music, mic)))

    @Test
    fun sceneKeyIsOnAirOnlyForItsScene() {
        assertEquals(KeyState.ON_AIR, scene.state(LiveState(currentScene = "InGame Scene")))
        assertNull(scene.state(LiveState(currentScene = "BRB")))
        assertNull(scene.state(LiveState()))
    }

    @Test
    fun inputKeyFollowsItsMuteState() {
        assertEquals(KeyState.ON, music.state(LiveState(muted = mapOf("Music" to false))))
        assertEquals(KeyState.OFF, music.state(LiveState(muted = mapOf("Music" to true))))
        assertNull(music.state(LiveState(muted = mapOf("Other" to true))))
    }

    @Test
    fun onlyOnAirAndUnmutedUseTheOnArt() {
        assertTrue(KeyState.ON_AIR.isOn())
        assertTrue(KeyState.ON.isOn())
        assertFalse(KeyState.OFF.isOn())
        assertFalse((null as KeyState?).isOn())
    }

    @Test
    fun talkBackHearsNameThenState() {
        assertEquals("Microphone, live", spokenKey("Microphone", KeyState.ON.spoken(DeckKeyArt.MIC)))
        assertEquals("Microphone, muted", spokenKey("Microphone", KeyState.OFF.spoken(DeckKeyArt.MIC)))
        assertEquals("Music, off, not connected", spokenKey("Music", KeyState.OFF.spoken(DeckKeyArt.MUSIC), offline = true))
        assertEquals("Gaming, on air", spokenKey("Gaming", KeyState.ON_AIR.spoken(DeckKeyArt.GAMING)))
        assertEquals("Applause, no action", spokenKey("Applause", null, "no action"))
    }

    @Test
    fun onAirChipNamesTheKeyOrFallsBackToTheScene() {
        assertEquals("Gaming", onAirChipText(pages, LiveState(currentScene = "InGame Scene")))
        assertEquals("Other scene", onAirChipText(pages, LiveState(currentScene = "Other scene")))
        assertNull(onAirChipText(pages, LiveState()))
    }

    @Test
    fun micChipFollowsTheFirstMicKeyWithAnInput() {
        assertEquals(true, micChipLive(pages, LiveState(muted = mapOf("Mic/Aux" to false))))
        assertEquals(false, micChipLive(pages, LiveState(muted = mapOf("Mic/Aux" to true))))
        assertNull(micChipLive(pages, LiveState()))
        assertNull(micChipLive(listOf(DeckPage("p", "Main", PageIcon.GRID, listOf(music))), LiveState(muted = mapOf("Music" to false))))
    }

    @Test
    fun emptySlotsFillTheGridAndAlwaysLeaveOne() {
        assertEquals(9, emptySlotCount(6, 3))       // mockup 02: 6 keys + 9 slots = 3 × 5
        assertEquals(15, emptySlotCount(0, 3))
        assertEquals(3, emptySlotCount(15, 3))      // full page: one more row
        assertEquals(2, emptySlotCount(16, 3))      // finish the row
        assertEquals(24, emptySlotCount(0, 4))      // 4 × 6
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
}
