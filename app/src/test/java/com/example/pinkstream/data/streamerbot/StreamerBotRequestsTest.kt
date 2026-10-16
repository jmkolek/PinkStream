package com.example.pinkstream.data.streamerbot

import org.junit.Assert.assertEquals
import org.junit.Test

class StreamerBotRequestsTest {

    @Test
    fun getActionsJson() {
        assertEquals(
            """{"request":"GetActions","id":"req-1"}""",
            StreamerBotRequests.getActions("req-1"),
        )
    }

    @Test
    fun doActionWithIdAndName() {
        assertEquals(
            """{"request":"DoAction","id":"req-2","action":{"id":"guid-1","name":"Deck - Scene Gaming"},"args":{}}""",
            StreamerBotRequests.doAction("req-2", actionId = "guid-1", actionName = "Deck - Scene Gaming"),
        )
    }

    @Test
    fun doActionWithNameOnlyAndArgs() {
        assertEquals(
            """{"request":"DoAction","id":"req-3","action":{"name":"Deck - Test"},"args":{"source":"phone"}}""",
            StreamerBotRequests.doAction("req-3", actionId = null, actionName = "Deck - Test", args = mapOf("source" to "phone")),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun doActionNeedsIdOrName() {
        StreamerBotRequests.doAction("req-4", actionId = null, actionName = null)
    }

    @Test
    fun authenticateJson() {
        assertEquals(
            """{"request":"Authenticate","id":"req-5","authentication":"abc="}""",
            StreamerBotRequests.authenticate("req-5", "abc="),
        )
    }
}
