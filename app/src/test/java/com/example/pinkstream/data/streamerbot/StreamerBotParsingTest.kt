package com.example.pinkstream.data.streamerbot

import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamerBotParsingTest {

    @Test
    fun parsesGetActionsExample() {
        // The example from docs/PLAN.md → Protocol reference, plus an unknown field.
        val json = """
            {
              "id": "req-1",
              "status": "ok",
              "count": 1,
              "somethingNew": true,
              "actions": [
                { "id": "guid-1", "name": "Deck - Test", "group": "Stream Deck", "enabled": true, "subaction_count": 1 }
              ]
            }
        """.trimIndent()

        val response = StreamerBotJson.decodeFromString(GetActionsResponse.serializer(), json)

        assertEquals("ok", response.status)
        assertEquals(1, response.count)
        assertEquals(
            listOf(ActionInfo(id = "guid-1", name = "Deck - Test", group = "Stream Deck", enabled = true, subactionCount = 1)),
            response.actions,
        )
    }

    @Test
    fun parsesEventEnvelope() {
        val json = """
            {"timeStamp": "2026-10-16T21:00:00Z", "event": {"source": "Obs", "type": "SceneChanged"}, "data": {"scene": "Gaming"}}
        """.trimIndent()

        val envelope = StreamerBotJson.decodeFromString(EventEnvelope.serializer(), json)

        assertEquals("Obs", envelope.event.source)
        assertEquals("SceneChanged", envelope.event.type)
        assertEquals(JsonPrimitive("Gaming"), envelope.data?.get("scene"))
    }

    @Test
    fun parsesHelloWithAndWithoutAuthentication() {
        val withAuth = """
            {"request":"Hello","session":"s","info":{"name":"Streamer.bot","version":"1.0.0","darkMode":true},
             "authentication":{"salt":"Zm9vYmFyc2FsdA==","challenge":"Y2hhbGxlbmdlMTIz"}}
        """.trimIndent()
        val hello = StreamerBotJson.decodeFromString(Hello.serializer(), withAuth)
        assertEquals("1.0.0", hello.info?.version)
        assertEquals("Zm9vYmFyc2FsdA==", hello.authentication?.salt)

        val noAuth = StreamerBotJson.decodeFromString(Hello.serializer(), """{"request":"Hello"}""")
        assertNull(noAuth.authentication)
    }

    @Test
    fun genericResponseErrors() {
        val ok = StreamerBotJson.decodeFromString(Response.serializer(), """{"id":"1","status":"ok"}""")
        assertTrue(ok.isOk)
        assertNull(ok.errorMessage)

        val error = StreamerBotJson.decodeFromString(Response.serializer(), """{"id":"2","status":"error","error":"Action not found"}""")
        assertFalse(error.isOk)
        assertEquals("Action not found", error.errorMessage)

        val objectError = StreamerBotJson.decodeFromString(Response.serializer(), """{"id":"3","status":"error","error":{"code":4}}""")
        assertNotNull(objectError.errorMessage)
    }

    @Test
    fun masksAuthenticationString() {
        val masked = maskSecrets("""{"request":"Authenticate","id":"1","authentication":"secret+value="}""")
        assertEquals("""{"request":"Authenticate","id":"1","authentication":"***"}""", masked)

        // The server's Hello carries an object, not a secret: leave it alone.
        val hello = """{"authentication":{"salt":"a","challenge":"b"}}"""
        assertEquals(hello, maskSecrets(hello))
    }
}
