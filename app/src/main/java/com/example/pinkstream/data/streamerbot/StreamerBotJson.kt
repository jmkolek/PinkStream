package com.example.pinkstream.data.streamerbot

import kotlinx.serialization.json.Json

/**
 * One shared Json for the Streamer.bot protocol. Lenient on purpose: several payloads
 * are undocumented, so unknown fields are skipped and missing ones become null/defaults.
 */
val StreamerBotJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
