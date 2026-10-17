package com.example.pinkstream.ui.edit

import com.example.pinkstream.data.deck.DeckButton
import com.example.pinkstream.ui.icons.DeckIcon
import com.example.pinkstream.ui.theme.TileColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ButtonDraftTest {

    private val gaming = DeckButton(
        id = "b1", label = "Gaming", icon = DeckIcon.GAMEPAD, color = TileColor.ROSE,
        actionId = "g-1", actionName = "Deck - Scene Gaming", onAirScene = "Gaming",
    )

    @Test
    fun aButtonSurvivesTheRoundTripThroughTheForm() {
        assertEquals(gaming, gaming.toDraft().toButton("b1"))
    }

    @Test
    fun switchingOffClearsTheNameButKeepsItInTheForm() {
        val draft = gaming.toDraft().copy(onAir = false)
        assertNull(draft.toButton("b1").onAirScene)
        assertEquals("Gaming", draft.onAirScene)                 // back if switched on again
        assertEquals("Gaming", draft.withOnAir(true).toButton("b1").onAirScene)
    }

    @Test
    fun turningOnAirOnPrefillsTheLabel() {
        val draft = newButtonDraft(TileColor.PEACH).copy(label = "  Just chatting ").withOnAir(true)
        assertEquals("Just chatting", draft.onAirScene)
    }

    @Test
    fun validationNeedsALabelAndTheNamesForSwitchedOnOptions() {
        val empty = newButtonDraft(TileColor.ROSE)
        assertNotNull(empty.validate().label)
        assertFalse(empty.validate().ok)

        val missingNames = empty.copy(label = "Desktop", onAir = true, showState = true)
        val errors = missingNames.validate()
        assertNull(errors.label)
        assertNotNull(errors.onAirScene)
        assertNotNull(errors.stateInput)

        assertTrue(missingNames.copy(onAirScene = "Desktop", stateInput = "Desktop Audio").validate().ok)
    }

    @Test
    fun textIsTrimmedOnSave() {
        val draft = gaming.toDraft().copy(label = " Gaming ", showState = true, stateInput = " Mic/Aux ")
        val saved = draft.toButton("b1")
        assertEquals("Gaming", saved.label)
        assertEquals("Mic/Aux", saved.stateInput)
    }

    @Test
    fun iconRowStartsWithTheCurrentIconAndStaysPut() {
        assertEquals(listOf(DeckIcon.GAMEPAD, DeckIcon.CHAT, DeckIcon.COFFEE, DeckIcon.MOON, DeckIcon.SPARKLES), iconRow(DeckIcon.GAMEPAD))
        assertEquals(listOf(DeckIcon.FLAG, DeckIcon.GAMEPAD, DeckIcon.CHAT, DeckIcon.COFFEE, DeckIcon.MOON), iconRow(DeckIcon.FLAG))

        val row = iconRow(DeckIcon.GAMEPAD)
        assertEquals(row, row.withChoice(DeckIcon.MOON))           // already there: no reshuffle
        assertEquals(DeckIcon.STAR, row.withChoice(DeckIcon.STAR).first())
        assertEquals(ICON_ROW_SIZE, row.withChoice(DeckIcon.STAR).size)
    }
}
