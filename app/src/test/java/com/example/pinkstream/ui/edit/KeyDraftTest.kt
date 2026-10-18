package com.example.pinkstream.ui.edit

import com.example.pinkstream.data.deck.DeckKey
import com.example.pinkstream.ui.icons.DeckKeyArt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyDraftTest {

    private val gaming = DeckKey(
        id = "k1", label = "Gaming", art = DeckKeyArt.GAMING,
        actionId = "g-1", actionName = "Deck - Scene Gaming", onAirScene = "Gaming",
    )

    @Test
    fun aKeySurvivesTheRoundTripThroughTheForm() {
        assertEquals(gaming, gaming.toDraft().toKey("k1"))
    }

    @Test
    fun switchingOffClearsTheNameButKeepsItInTheForm() {
        val draft = gaming.toDraft().copy(onAir = false)
        assertNull(draft.toKey("k1").onAirScene)
        assertEquals("Gaming", draft.onAirScene)                 // back if switched on again
        assertEquals("Gaming", draft.withOnAir(true).toKey("k1").onAirScene)
    }

    @Test
    fun turningOnAirOnPrefillsTheName() {
        val draft = newKeyDraft().copy(label = "  Just chatting ").withOnAir(true)
        assertEquals("Just chatting", draft.onAirScene)
    }

    @Test
    fun validationNeedsANameAndTheNamesForSwitchedOnOptions() {
        val empty = newKeyDraft()
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
        val saved = draft.toKey("k1")
        assertEquals("Gaming", saved.label)
        assertEquals("Mic/Aux", saved.stateInput)
    }

    @Test
    fun aKeyMadeFromAnActionIsPrefilled() {
        val scene = newKeyDraftFor("g-1", "Deck - Scene Gaming")
        assertEquals("Gaming", scene.label)
        assertEquals(DeckKeyArt.GAMING, scene.art)
        assertEquals("g-1", scene.actionId)
        assertEquals("Deck - Scene Gaming", scene.actionName)
        assertTrue(scene.onAir)
        assertEquals("Gaming", scene.onAirScene)
        assertFalse(scene.showState)

        val mic = newKeyDraftFor("m-1", "Deck - Mic Toggle")
        assertEquals(DeckKeyArt.MIC, mic.art)
        assertFalse(mic.onAir)
        assertTrue(mic.showState)
        assertEquals("Mic/Aux", mic.stateInput)
        assertEquals("Desktop Audio", newKeyDraftFor(null, "Deck - Desktop Toggle").stateInput)

        // Nothing to go on: the name as it is and the first art.
        val other = newKeyDraftFor("t-1", "Deck - Test")
        assertEquals("Test", other.label)
        assertEquals(DeckKeyArt.entries.first(), other.art)
        assertFalse(other.onAir || other.showState)
        assertTrue(other.validate().ok)
    }

    @Test
    fun previewNamesTheOnArtAfterWhatItFollows() {
        assertEquals("On air", gaming.toDraft().onArtLabel())
        assertEquals("On", newKeyDraft().copy(showState = true).onArtLabel())
        assertEquals("Pressed", newKeyDraft().onArtLabel())
    }
}
