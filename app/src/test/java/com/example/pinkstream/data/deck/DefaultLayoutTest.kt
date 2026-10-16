package com.example.pinkstream.data.deck

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DefaultLayoutTest {

    private val layout = DefaultLayout.create()

    @Test
    fun hasThePlannedPagesAndSections() {
        assertEquals(listOf("Main", "Model", "Extras"), layout.pages.map { it.name })
        val main = layout.pages.first()
        assertEquals(listOf(null, "Scenes", "Sound", "Model"), main.sections.map { it.title })
        assertEquals(SectionStyle.HERO, main.sections.first().style)
        assertEquals(listOf(1, 6, 3, 3), main.sections.map { it.buttons.size })
    }

    @Test
    fun everyPageSectionAndButtonHasItsOwnId() {
        val ids = layout.pages.flatMap { page ->
            listOf(page.id) + page.sections.flatMap { section -> listOf(section.id) + section.buttons.map { it.id } }
        }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun buttonsStartWithANameButNoActionId() {
        val buttons = layout.pages.flatMap { p -> p.sections.flatMap { it.buttons } }
        buttons.forEach { assertNull(it.actionId) }
        assertEquals("Deck - Scene Gaming", buttons.single { it.label == "Gaming" }.actionName)
        assertEquals("Gaming", buttons.single { it.label == "Gaming" }.onAirScene)
        assertEquals("Mic/Aux", buttons.single { it.label == "Microphone" }.stateInput)
    }

    @Test
    fun survivesAJsonRoundTrip() {
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        val text = json.encodeToString(DeckLayout.serializer(), layout)
        assertEquals(layout, json.decodeFromString(DeckLayout.serializer(), text))
    }
}
