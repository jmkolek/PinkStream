package com.example.pinkstream.data.deck

import kotlinx.serialization.json.Json

/**
 * The layout as a backup file: the same JSON the app stores, pretty-printed so it can
 * be read (and carefully edited) on a PC.
 */
object LayoutBackup {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun encode(layout: DeckLayout): String = json.encodeToString(DeckLayout.serializer(), layout)

    /** The layout in [text], or null if it isn't one we can use safely. */
    fun decode(text: String): DeckLayout? {
        val layout = try {
            json.decodeFromString(DeckLayout.serializer(), text)
        } catch (e: IllegalArgumentException) {   // SerializationException is one of these
            return null
        }
        return layout.takeIf { it.isUsable() }
    }

    /**
     * At least one page, and every page, section and button id present and unique:
     * editing and removing find things by id, so a hand-edited file with a copied
     * button would otherwise change two tiles at once.
     */
    private fun DeckLayout.isUsable(): Boolean {
        if (pages.isEmpty()) return false
        val ids = pages.flatMap { page ->
            listOf(page.id) + page.sections.flatMap { section -> listOf(section.id) + section.buttons.map { it.id } }
        }
        return ids.none { it.isBlank() } && ids.size == ids.toSet().size
    }
}
