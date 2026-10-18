package com.example.pinkstream.data.deck

import com.example.pinkstream.ui.icons.PageIcon
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultLayoutTest {

    private val layout = DefaultLayout.create()

    @Test
    fun startsWithThreeEmptyPages() {
        assertEquals(listOf("Main", "Model", "Extras"), layout.pages.map { it.name })
        assertEquals(listOf(PageIcon.GRID, PageIcon.SMILE, PageIcon.SPARKLES), layout.pages.map { it.icon })
        assertEquals(emptyList<DeckKey>(), layout.allKeys)
    }

    @Test
    fun everyPageHasItsOwnId() {
        val ids = layout.pages.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun survivesAJsonRoundTrip() {
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        val text = json.encodeToString(DeckLayout.serializer(), layout)
        assertEquals(layout, json.decodeFromString(DeckLayout.serializer(), text))
    }
}
