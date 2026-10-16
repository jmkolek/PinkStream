package com.example.pinkstream.data.streamerbot

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/** Builds the JSON text of each request. Every request carries a unique `id`. */
object StreamerBotRequests {

    fun getActions(id: String): String = buildJsonObject {
        put("request", "GetActions")
        put("id", id)
    }.toString()

    /** Send the action id and name when both are known; either one alone works. */
    fun doAction(id: String, actionId: String?, actionName: String?, args: Map<String, String> = emptyMap()): String {
        require(actionId != null || actionName != null) { "DoAction needs an action id or name" }
        return buildJsonObject {
            put("request", "DoAction")
            put("id", id)
            putJsonObject("action") {
                if (actionId != null) put("id", actionId)
                if (actionName != null) put("name", actionName)
            }
            putJsonObject("args") {
                args.forEach { (key, value) -> put(key, value) }
            }
        }.toString()
    }

    fun authenticate(id: String, authentication: String): String = buildJsonObject {
        put("request", "Authenticate")
        put("id", id)
        put("authentication", authentication)
    }.toString()
}
