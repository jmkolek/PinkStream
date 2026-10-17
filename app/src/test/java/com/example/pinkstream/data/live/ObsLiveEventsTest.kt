package com.example.pinkstream.data.live

import com.example.pinkstream.data.obs.ObsEvent
import com.example.pinkstream.data.streamerbot.StreamerBotJson
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObsLiveEventsTest {

    private fun event(type: String, data: String) = ObsEvent(type, StreamerBotJson.parseToJsonElement(data).jsonObject)

    private val start = LiveState(currentScene = "Gaming", muted = mapOf("Mic/Aux" to false, "Desktop Audio" to false))

    @Test
    fun programSceneChange() {
        val e = event("CurrentProgramSceneChanged", """{"sceneName":"BRB","sceneUuid":"u1"}""")
        assertEquals("BRB", ObsLiveEvents.apply(start, e).currentScene)
        assertTrue(ObsLiveEvents.isSceneChange(e))
    }

    @Test
    fun muteChangeUpdatesOnlyThatInput() {
        val e = event("InputMuteStateChanged", """{"inputName":"Mic/Aux","inputUuid":"u2","inputMuted":true}""")
        val after = ObsLiveEvents.apply(start, e)
        assertEquals(mapOf("Mic/Aux" to true, "Desktop Audio" to false), after.muted)
        assertEquals("Gaming", after.currentScene)
        assertFalse(ObsLiveEvents.isSceneChange(e))
    }

    @Test
    fun muteChangeForANewInputAddsIt() {
        val e = event("InputMuteStateChanged", """{"inputName":"Music","inputMuted":false}""")
        assertEquals(false, ObsLiveEvents.apply(start, e).muted["Music"])
    }

    @Test
    fun renamedInputKeepsItsState() {
        val e = event("InputNameChanged", """{"inputUuid":"u2","oldInputName":"Mic/Aux","inputName":"Mic"}""")
        assertEquals(mapOf("Mic" to false, "Desktop Audio" to false), ObsLiveEvents.apply(start, e).muted)
    }

    @Test
    fun removedInputBecomesUnknown() {
        val e = event("InputRemoved", """{"inputName":"Desktop Audio","inputUuid":"u3"}""")
        assertEquals(mapOf("Mic/Aux" to false), ObsLiveEvents.apply(start, e).muted)
    }

    @Test
    fun oddOrOtherEventsChangeNothing() {
        assertEquals(start, ObsLiveEvents.apply(start, event("InputMuteStateChanged", """{"inputName":"Mic/Aux"}""")))
        assertEquals(start, ObsLiveEvents.apply(start, event("CurrentProgramSceneChanged", """{"sceneName":7}""")))
        assertEquals(start, ObsLiveEvents.apply(start, event("InputVolumeChanged", """{"inputName":"Mic/Aux","inputVolumeDb":-3.0}""")))
    }
}
