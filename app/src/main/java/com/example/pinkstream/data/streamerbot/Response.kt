package com.example.pinkstream.data.streamerbot

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/** The part every response shares. It echoes the request's `id`. */
@Serializable
data class Response(
    val id: String? = null,
    val status: String? = null,
    // Undocumented shape: may be a string or an object, so keep it as raw JSON.
    val error: JsonElement? = null,
) {
    val isOk: Boolean get() = status == "ok"

    /** The error as readable text, or null if there isn't one. */
    val errorMessage: String?
        get() = when (val e = error) {
            null -> null
            is JsonPrimitive -> e.content.takeIf { it.isNotBlank() && it != "null" }
            else -> e.toString()
        }
}
