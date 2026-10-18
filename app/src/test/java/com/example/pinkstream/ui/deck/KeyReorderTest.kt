package com.example.pinkstream.ui.deck

import androidx.compose.ui.geometry.Offset
import com.example.pinkstream.data.deck.moved
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyReorderTest {

    // 3 columns, 100 px keys with a 20 px gap: one step is 120 px.
    private val step = 120f
    private val size = 100f

    @Test
    fun slotsFillRowsLeftToRight() {
        assertEquals(Offset(0f, 0f), slotOffset(0, 3, step))
        assertEquals(Offset(240f, 0f), slotOffset(2, 3, step))
        assertEquals(Offset(0f, 120f), slotOffset(3, 3, step))
        assertEquals(Offset(120f, 240f), slotOffset(7, 3, step))
    }

    @Test
    fun slotAtStaysWithinThePagesKeys() {
        assertEquals(4, slotAt(170f, 170f, step, 3, 10))
        assertEquals(0, slotAt(-50f, -50f, step, 3, 10))        // above or left of the grid
        assertEquals(2, slotAt(900f, 10f, step, 3, 10))         // right of the last column
        assertEquals(9, slotAt(10f, 2000f, step, 3, 10))        // below the last key
        assertEquals(0, slotAt(10f, 10f, step, 3, 0))           // no keys at all
    }

    @Test
    fun keysInBetweenShiftToMakeRoom() {
        // Key 1 held over slot 4: 2, 3, 4 move back one; the rest stay.
        assertEquals(listOf(0, 4, 1, 2, 3, 5), (0..5).map { shownIndex(it, from = 1, to = 4) })
        // Key 4 held over slot 1: 1, 2, 3 move on one.
        assertEquals(listOf(0, 2, 3, 4, 1, 5), (0..5).map { shownIndex(it, from = 4, to = 1) })
        assertEquals(listOf(0, 1, 2), (0..2).map { shownIndex(it, from = 1, to = 1) })
    }

    @Test
    fun shownOrderMatchesTheSavedOrderAfterTheMove() {
        val keys = listOf("a", "b", "c", "d", "e")
        val after = keys.moved(1, 3)
        assertEquals(listOf("a", "c", "d", "b", "e"), after)
        keys.forEachIndexed { index, key -> assertEquals(after.indexOf(key), shownIndex(index, 1, 3)) }
    }

    @Test
    fun aDragFollowsTheFingerAndDropsWhereItIs() {
        val drag = KeyDrag()
        drag.start("b", 1)
        assertFalse(drag.dragBy(Offset(30f, 0f), step, size, 3, 6))   // still over its own slot
        assertTrue(drag.dragBy(Offset(0f, 120f), step, size, 3, 6))   // one row down: slot 4
        assertEquals(4, drag.to)
        assertEquals(3, drag.shown(4))                                // the key that was there moves back one...
        assertEquals(4, drag.shown(1))                                // ...and the held key shows over slot 4
        assertEquals(1 to 4, drag.drop())
        assertTrue(drag.settling)
        assertNull(drag.keyId)
        drag.reset()
        assertEquals(3, drag.shown(3))
    }

    @Test
    fun droppingBackInPlaceIsNoMove() {
        val drag = KeyDrag()
        drag.start("a", 0)
        drag.dragBy(Offset(10f, 10f), step, size, 3, 6)
        assertNull(drag.drop())
        assertFalse(drag.settling)
    }
}
