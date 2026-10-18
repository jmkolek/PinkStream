package com.example.pinkstream.ui.edit

import com.example.pinkstream.data.deck.DeckKey
import com.example.pinkstream.data.deck.DeckLayout
import com.example.pinkstream.data.deck.DeckPage
import com.example.pinkstream.data.streamerbot.ActionInfo
import com.example.pinkstream.ui.icons.DeckIcon
import com.example.pinkstream.ui.icons.DeckKeyArt
import com.example.pinkstream.ui.icons.PageIcon
import com.example.pinkstream.ui.theme.IconTint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ActionPickingTest {

    private val actions = listOf(
        ActionInfo("1", "Deck - Scene Gaming", "Stream Deck"),
        ActionInfo("2", "Deck - Mic Toggle", "Stream Deck"),
        ActionInfo("3", "Deck - VTS Happy", "Stream Deck"),
        ActionInfo("4", "Deck - Test", "Stream Deck"),
        ActionInfo("5", "Raid message", "Twitch"),
        ActionInfo("6", "deck - scene brb", "Stream Deck"),
    )

    @Test
    fun guessesIconAndColourFromTheName() {
        assertEquals(ActionStyle(DeckIcon.MONITOR, IconTint.ROSE), guessActionStyle("Deck - Scene Gaming"))
        assertEquals(ActionStyle(DeckIcon.VOLUME, IconTint.PEACH), guessActionStyle("Deck - Mic Toggle"))
        assertEquals(ActionStyle(DeckIcon.VOLUME, IconTint.PEACH), guessActionStyle("Deck - Music Toggle"))
        assertEquals(ActionStyle(DeckIcon.SMILE, IconTint.LILAC), guessActionStyle("Deck - VTS Happy"))
        assertEquals(ActionStyle(DeckIcon.BOLT, IconTint.MIST), guessActionStyle("Deck - Applause"))
    }

    @Test
    fun searchIgnoresCaseAndResultsAreSortedByName() {
        val names = filterActions(actions, "SCENE", group = null).map { it.name }
        assertEquals(listOf("deck - scene brb", "Deck - Scene Gaming"), names)
    }

    @Test
    fun groupFilterAndDefaultGroup() {
        assertEquals(listOf("Raid message"), filterActions(actions, "", "Twitch").map { it.name })
        assertEquals(5, filterActions(actions, "", STREAM_DECK_GROUP).size)
        assertEquals(STREAM_DECK_GROUP, defaultGroup(actions))
        assertNull(defaultGroup(listOf(ActionInfo("5", "Raid message", "Twitch"))))
    }

    @Test
    fun matchRangesFindsEveryMatch() {
        assertEquals(listOf(7..11), matchRanges("Deck - Scene Gaming", "scene"))
        assertEquals(listOf(0..1, 4..5), matchRanges("abxxab", "AB"))
        assertEquals(emptyList<IntRange>(), matchRanges("Deck", "  "))
    }

    @Test
    fun usageLineNamesTheButton() {
        val brb = DeckKey("b2", "BRB", DeckKeyArt.BRB, actionId = "6")
        val layout = DeckLayout(listOf(DeckPage("p", "Main", PageIcon.GRID, listOf(brb))))

        assertEquals("On this button", actionUsage(layout, actions[0], isThisKeysAction = true, thisKeyId = "b1"))
        assertEquals("On the BRB button", actionUsage(layout, actions[5], isThisKeysAction = false, thisKeyId = "b1"))
        assertEquals("Not on a button yet", actionUsage(layout, actions[4], isThisKeysAction = false, thisKeyId = "b1"))
        // Editing BRB itself and the form now points elsewhere: BRB's old action isn't "on" another key.
        assertEquals("Not on a button yet", actionUsage(layout, actions[5], isThisKeysAction = false, thisKeyId = "b2"))
    }

    @Test
    fun keyNameDropsTheDeckPrefixAndCategoryWord() {
        assertEquals("Gaming", keyNameFor("Deck - Scene Gaming"))
        assertEquals("Happy", keyNameFor("Deck - VTS Happy"))
        assertEquals("Mic Toggle", keyNameFor("Deck – Mic Toggle"))
        assertEquals("Raid message", keyNameFor("Raid message"))
        assertEquals("Deck -", keyNameFor("Deck -"))        // nothing left: keep the action's name
    }
}
