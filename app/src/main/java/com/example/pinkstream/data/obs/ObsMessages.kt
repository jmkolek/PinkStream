package com.example.pinkstream.data.obs

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * The OBS WebSocket 5 protocol (github.com/obsproject/obs-websocket, docs/generated/protocol.md).
 * Every message is `{"op": <number>, "d": {…}}`:
 *   0 Hello (OBS → app)       1 Identify (app → OBS)      2 Identified (OBS → app)
 *   5 Event (OBS → app)       6 Request (app → OBS)       7 RequestResponse (OBS → app)
 * The app only sends "Get…" requests, so it can never change anything in OBS.
 */
object ObsMessages {

    const val OP_HELLO = 0
    const val OP_IDENTIFIED = 2
    const val OP_EVENT = 5
    const val OP_REQUEST_RESPONSE = 7

    // Event groups we ask for, as bit flags: Scenes has CurrentProgramSceneChanged,
    // Inputs has InputMuteStateChanged (and renames/removals). The noisy volume meters
    // are a separate group we leave out.
    private const val EVENTS_SCENES = 1 shl 2
    private const val EVENTS_INPUTS = 1 shl 3
    const val EVENT_SUBSCRIPTIONS = EVENTS_SCENES or EVENTS_INPUTS

    /** OBS closes the socket with this code when the password is wrong. */
    const val CLOSE_AUTHENTICATION_FAILED = 4009

    /** [authentication] is null when OBS didn't ask for a password. */
    fun identify(authentication: String?): String = buildJsonObject {
        put("op", 1)
        put("d", buildJsonObject {
            put("rpcVersion", 1)
            if (authentication != null) put("authentication", authentication)
            put("eventSubscriptions", EVENT_SUBSCRIPTIONS)
        })
    }.toString()

    fun request(requestId: String, requestType: String, requestData: JsonObject? = null): String = buildJsonObject {
        put("op", 6)
        put("d", buildJsonObject {
            put("requestType", requestType)
            put("requestId", requestId)
            if (requestData != null) put("requestData", requestData)
        })
    }.toString()

    // --- Reading answers. All lenient: a missing or odd field gives null. ---------------

    /** GetCurrentProgramScene's responseData → the scene name. */
    fun programScene(responseData: JsonObject?): String? = responseData?.string("currentProgramSceneName")

    /** GetInputList's responseData → every input's name. */
    fun inputNames(responseData: JsonObject?): List<String> =
        (responseData?.get("inputs") as? JsonArray).orEmpty()
            .mapNotNull { (it as? JsonObject)?.string("inputName") }

    /** GetInputMute's responseData → muted or not. */
    fun inputMuted(responseData: JsonObject?): Boolean? =
        (responseData?.get("inputMuted") as? JsonPrimitive)?.booleanOrNull

    fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content
}
