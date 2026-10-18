package com.example.pinkstream.ui.icons

import org.junit.Assert.assertEquals
import org.junit.Test

class DeckKeyArtTest {

    @Test
    fun blankSearchListsEveryDesignInCatalogOrder() {
        assertEquals(DeckKeyArt.entries, DeckKeyArt.search(""))
        assertEquals(DeckKeyArt.entries, DeckKeyArt.search("   "))
    }

    @Test
    fun searchMatchesPartOfTheNameIgnoringCase() {
        assertEquals(listOf(DeckKeyArt.MIC), DeckKeyArt.search("  MICRO "))
        assertEquals(
            listOf(DeckKeyArt.HIGHLIGHT, DeckKeyArt.RING_LIGHT, DeckKeyArt.LIGHTS),
            DeckKeyArt.search("light"),
        )
    }

    @Test
    fun searchDoesNotMatchGroupNames() {
        assertEquals(emptyList<DeckKeyArt>(), DeckKeyArt.search("Scenes"))
    }
}
