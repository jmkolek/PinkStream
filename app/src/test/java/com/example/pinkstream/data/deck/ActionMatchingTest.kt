package com.example.pinkstream.data.deck

import com.example.pinkstream.data.streamerbot.ActionInfo
import com.example.pinkstream.ui.icons.DeckKeyArt
import com.example.pinkstream.ui.icons.PageIcon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionMatchingTest {

    private val gaming = ActionInfo(id = "g-1", name = "Deck - Scene Gaming")
    private val test = ActionInfo(id = "t-1", name = "Deck - Test")

    private fun button(actionId: String? = null, actionName: String? = null) =
        DeckKey(id = "b", label = "Gaming", art = DeckKeyArt.GAMING, actionId = actionId, actionName = actionName)

    private fun layoutOf(vararg keys: DeckKey) =
        DeckLayout(listOf(DeckPage("p", "Main", PageIcon.GRID, keys.toList())))

    private fun DeckLayout.onlyButton() = pages.single().keys.single()

    @Test
    fun fillsAMissingIdByExactName() {
        val updated = layoutOf(button(actionName = "Deck - Scene Gaming")).withActionIds(listOf(test, gaming))
        assertEquals("g-1", updated.onlyButton().actionId)
    }

    @Test
    fun nameMatchIsExact() {
        val updated = layoutOf(button(actionName = "deck - scene gaming")).withActionIds(listOf(gaming))
        assertNull(updated.onlyButton().actionId)
    }

    @Test
    fun replacesAnIdThatNoLongerExists() {
        val updated = layoutOf(button(actionId = "deleted", actionName = "Deck - Scene Gaming")).withActionIds(listOf(gaming))
        assertEquals("g-1", updated.onlyButton().actionId)
    }

    @Test
    fun keepsAWorkingIdEvenIfTheNameChanged() {
        val renamed = ActionInfo(id = "g-1", name = "Gaming scene")
        val layout = layoutOf(button(actionId = "g-1", actionName = "Deck - Scene Gaming"))
        assertSame(layout, layout.withActionIds(listOf(renamed)))
    }

    @Test
    fun returnsTheSameObjectWhenNothingChanged() {
        val layout = layoutOf(button(actionName = "Deck - Missing"))
        assertSame(layout, layout.withActionIds(listOf(gaming)))
    }

    @Test
    fun hasActionChecksIdThenName() {
        assertTrue(button(actionName = "Deck - Scene Gaming").hasAction(null))      // list unknown: assume fine
        assertFalse(button().hasAction(null))                                      // nothing assigned at all
        assertTrue(button(actionId = "g-1").hasAction(listOf(gaming)))
        assertTrue(button(actionName = "Deck - Scene Gaming").hasAction(listOf(gaming)))
        assertFalse(button(actionId = "x", actionName = "Deck - Missing").hasAction(listOf(gaming)))
    }
}
