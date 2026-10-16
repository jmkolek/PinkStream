package com.example.pinkstream.data.streamerbot

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * How every subscribed event arrives:
 * `{"timeStamp": …, "event": {"source": "Obs", "type": "SceneChanged"}, "data": {…}}`.
 * `data` stays raw JSON until milestone 5 finds out what's in it.
 */
@Serializable
data class EventEnvelope(
    val timeStamp: JsonElement? = null,   // format unverified (string or number), so kept raw
    val event: EventInfo,
    val data: JsonObject? = null,
)

@Serializable
data class EventInfo(
    val source: String,
    val type: String,
)
