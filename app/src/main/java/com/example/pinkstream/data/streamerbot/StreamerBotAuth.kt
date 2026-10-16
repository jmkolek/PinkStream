package com.example.pinkstream.data.streamerbot

import java.security.MessageDigest
import java.util.Base64

/**
 * Streamer.bot's challenge-response login:
 *   secret         = base64(sha256(password + salt))
 *   authentication = base64(sha256(secret + challenge))
 * Salt and challenge are used as the base64 text received, not decoded.
 */
object StreamerBotAuth {

    fun authentication(password: String, salt: String, challenge: String): String =
        sha256Base64(sha256Base64(password + salt) + challenge)

    fun sha256Base64(s: String): String =
        Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA-256").digest(s.toByteArray(Charsets.UTF_8))
        )
}
