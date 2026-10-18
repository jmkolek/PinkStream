package com.example.pinkstream.data.deck

import com.example.pinkstream.ui.icons.PageIcon

/**
 * The layout a fresh install starts with: three empty pages. Keys are made from the
 * actions Streamer.bot has (Edit layout → empty slot → Choose an action).
 */
object DefaultLayout {

    fun create(): DeckLayout = DeckLayout(
        pages = listOf(
            page("Main", PageIcon.GRID),
            page("Model", PageIcon.SMILE),
            page("Extras", PageIcon.SPARKLES),
        ),
    )

    private fun page(name: String, icon: PageIcon) =
        DeckPage(id = newDeckId(), name = name, icon = icon, keys = emptyList())
}
