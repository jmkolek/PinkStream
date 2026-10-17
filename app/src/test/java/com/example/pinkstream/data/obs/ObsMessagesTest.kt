package com.example.pinkstream.data.obs

import com.example.pinkstream.data.streamerbot.StreamerBotJson
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ObsMessagesTest {

    /** The responseData of a captured op 7 RequestResponse. */
    private fun responseData(json: String): JsonObject? =
        StreamerBotJson.parseToJsonElement(json).jsonObject["d"]!!.jsonObject["responseData"] as? JsonObject

    @Test
    fun identifyAsksForScenesAndInputs() {
        assertEquals(
            """{"op":1,"d":{"rpcVersion":1,"eventSubscriptions":12}}""",
            ObsMessages.identify(authentication = null),
        )
    }

    @Test
    fun identifyWithPassword() {
        assertEquals(
            """{"op":1,"d":{"rpcVersion":1,"authentication":"abc=","eventSubscriptions":12}}""",
            ObsMessages.identify(authentication = "abc="),
        )
    }

    @Test
    fun requestJson() {
        assertEquals(
            """{"op":6,"d":{"requestType":"GetCurrentProgramScene","requestId":"r1"}}""",
            ObsMessages.request("r1", "GetCurrentProgramScene"),
        )
        val data = StreamerBotJson.parseToJsonElement("""{"inputName":"Mic/Aux"}""").jsonObject
        assertEquals(
            """{"op":6,"d":{"requestType":"GetInputMute","requestId":"r2","requestData":{"inputName":"Mic/Aux"}}}""",
            ObsMessages.request("r2", "GetInputMute", data),
        )
    }

    // The fixtures below were captured from OBS 32.2.2 (WebSocket 5.7.4).

    @Test
    fun readsTheProgramScene() {
        val data = responseData(
            """{"d":{"requestId":"x","requestStatus":{"code":100,"result":true},"requestType":"GetCurrentProgramScene",
               "responseData":{"currentProgramSceneName":"Tajniaks - Japan Theme Stream End Scene","currentProgramSceneUuid":"1c68de5a",
               "sceneName":"Tajniaks - Japan Theme Stream End Scene","sceneUuid":"1c68de5a"}},"op":7}"""
        )
        assertEquals("Tajniaks - Japan Theme Stream End Scene", ObsMessages.programScene(data))
    }

    @Test
    fun readsInputNames() {
        val data = responseData(
            """{"d":{"requestId":"x","requestStatus":{"code":100,"result":true},"requestType":"GetInputList",
               "responseData":{"inputs":[
                 {"inputKind":"wasapi_output_capture","inputKindCaps":642,"inputName":"Desktop Audio","inputUuid":"32d2"},
                 {"inputKind":"wasapi_input_capture","inputKindCaps":130,"inputName":"Mic/Aux","inputUuid":"f81f"},
                 {"inputKind":"color_source_v3","inputKindCaps":32777,"inputName":"Color","inputUuid":"94d0"}]}},"op":7}"""
        )
        assertEquals(listOf("Desktop Audio", "Mic/Aux", "Color"), ObsMessages.inputNames(data))
    }

    @Test
    fun readsMuteState() {
        val data = responseData(
            """{"d":{"requestId":"x","requestStatus":{"code":100,"result":true},"requestType":"GetInputMute","responseData":{"inputMuted":true}},"op":7}"""
        )
        assertEquals(true, ObsMessages.inputMuted(data))
    }

    @Test
    fun missingAnswersGiveNothing() {
        // An input without audio: result false and no responseData.
        val data = responseData(
            """{"d":{"requestId":"x","requestStatus":{"code":604,"comment":"The specified input does not support audio.","result":false},"requestType":"GetInputMute"},"op":7}"""
        )
        assertNull(ObsMessages.inputMuted(data))
        assertNull(ObsMessages.programScene(null))
        assertEquals(emptyList<String>(), ObsMessages.inputNames(null))
    }
}
