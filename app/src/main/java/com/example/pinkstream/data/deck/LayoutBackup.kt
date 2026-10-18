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

    /**
     * The layout in [text], or null if it isn't one we can use safely. Files exported
     * before the key art redesign are moved over to the new shape.
     */
    fun decode(text: String): DeckLayout? =
        LayoutMigration.decode(json, text)?.layout?.takeIf { it.isUsable() }

    /**
     * At least one page, and every page and key id present and unique: editing and
     * removing find things by id, so a hand-edited file with a copied key would
     * otherwise change two keys at once.
     */
    private fun DeckLayout.isUsable(): Boolean {
        if (pages.isEmpty()) return false
        val ids = pages.flatMap { page -> listOf(page.id) + page.keys.map { it.id } }
        return ids.none { it.isBlank() } && ids.size == ids.toSet().size
    }
}
