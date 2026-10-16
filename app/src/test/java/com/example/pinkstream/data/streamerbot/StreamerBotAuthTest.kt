package com.example.pinkstream.data.streamerbot

import org.junit.Assert.assertEquals
import org.junit.Test

/** The test vector from docs/PLAN.md → Authenticate. */
class StreamerBotAuthTest {

    private val password = "deck-password"
    private val salt = "Zm9vYmFyc2FsdA=="
    private val challenge = "Y2hhbGxlbmdlMTIz"

    @Test
    fun secret() {
        assertEquals("SPbqzAcgp45N5YJSsr0tZie6mWHICnMcQ2PgHB5lKXE=", StreamerBotAuth.sha256Base64(password + salt))
    }

    @Test
    fun authentication() {
        assertEquals("1HTjDjwTxLiHVtNpD3FvwblXfo2HbjS2+ddhYIUIF0o=", StreamerBotAuth.authentication(password, salt, challenge))
    }
}
