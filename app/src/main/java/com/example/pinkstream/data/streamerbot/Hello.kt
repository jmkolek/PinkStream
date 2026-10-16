package com.example.pinkstream.data.streamerbot

import kotlinx.serialization.Serializable

/** Sent by Streamer.bot right after the socket opens. */
@Serializable
data class Hello(
    val request: String? = null,
    val session: String? = null,
    val info: ServerInfo? = null,
    /** Only present when authentication is on in Streamer.bot. */
    val authentication: AuthChallenge? = null,
)

@Serializable
data class ServerInfo(
    val instanceId: String? = null,
    val name: String? = null,
    val version: String? = null,
    val os: String? = null,
    val osVersion: String? = null,
    val mode: String? = null,
    val darkMode: Boolean? = null,
    val source: String? = null,
)

/** Both values are base64 text and are used exactly as received (see StreamerBotAuth). */
@Serializable
data class AuthChallenge(
    val salt: String,
    val challenge: String,
)
