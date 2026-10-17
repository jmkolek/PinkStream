package com.example.pinkstream.data.live

import com.example.pinkstream.data.streamerbot.EventEnvelope
import com.example.pinkstream.data.streamerbot.StreamerBotJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamerBotEventsTest {

    private fun event(json: String) = StreamerBotJson.decodeFromString(EventEnvelope.serializer(), json)

    // Captured from Streamer.bot 1.0.7 (the sceneItems lists cut down to one item).
    private val sceneChanged = event(
        """
        {"timeStamp":"2026-10-17T17:41:58.0256989+02:00","event":{"source":"Obs","type":"SceneChanged"},
         "data":{
           "oldScene":{"sceneName":"Tajniaks - Japan Theme InGame Scene","sceneItems":[{"inputKind":"monitor_capture","isGroup":false,"sceneItemId":1}]},
           "scene":{"sceneName":"Tajniaks - Japan Theme Talk Scene","sceneItems":[{"inputKind":"dshow_input","isGroup":false,"sceneItemId":1}]},
           "connection":{"id":"d118359c-7881-4a45-b145-1ca7f3391f81","host":"127.0.0.1","name":"my OBS","version":"32.2.2","websocketVersion":"5.7.4"}}}
        """.trimIndent()
    )

    @Test
    fun sceneChangedSetsTheNewScene() {
        val state = StreamerBotEvents.apply(LiveState(currentScene = "Tajniaks - Japan Theme InGame Scene"), sceneChanged)
        assertEquals("Tajniaks - Japan Theme Talk Scene", state.currentScene)
        assertTrue(StreamerBotEvents.isSceneChange(sceneChanged))
    }

    @Test
    fun sceneChangedKeepsMuteStates() {
        val before = LiveState(muted = mapOf("Mic/Aux" to true))
        assertEquals(mapOf("Mic/Aux" to true), StreamerBotEvents.apply(before, sceneChanged).muted)
    }

    @Test
    fun sceneChangedWithoutAUsableNameChangesNothing() {
        val before = LiveState(currentScene = "BRB")
        val odd = event("""{"event":{"source":"Obs","type":"SceneChanged"},"data":{"scene":{"sceneName":42}}}""")
        val empty = event("""{"event":{"source":"Obs","type":"SceneChanged"}}""")
        assertEquals(before, StreamerBotEvents.apply(before, odd))
        assertEquals(before, StreamerBotEvents.apply(before, empty))
    }

    @Test
    fun obsDisconnectingForgetsEverything() {
        val before = LiveState(currentScene = "BRB", muted = mapOf("Mic/Aux" to false))
        val disconnected = event("""{"event":{"source":"Obs","type":"Disconnected"},"data":{}}""")
        assertEquals(LiveState(), StreamerBotEvents.apply(before, disconnected))
    }

    @Test
    fun otherEventsChangeNothing() {
        val before = LiveState(currentScene = "BRB")
        val streaming = event("""{"event":{"source":"Obs","type":"StreamingStarted"},"data":{}}""")
        val twitch = event("""{"event":{"source":"Twitch","type":"SceneChanged"},"data":{"scene":{"sceneName":"x"}}}""")
        assertEquals(before, StreamerBotEvents.apply(before, streaming))
        assertEquals(before, StreamerBotEvents.apply(before, twitch))
        assertFalse(StreamerBotEvents.isSceneChange(twitch))
    }
}
