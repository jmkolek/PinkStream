package com.example.pinkstream.data.deck

import com.example.pinkstream.data.streamerbot.ActionInfo
import com.example.pinkstream.ui.icons.DeckIcon
import com.example.pinkstream.ui.theme.TileColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class LayoutEditsTest {

    private fun button(id: String, actionId: String? = null, actionName: String? = null) =
        DeckButton(id, "Button $id", DeckIcon.BOLT, TileColor.ROSE, actionId = actionId, actionName = actionName)

    private val layout = DeckLayout(
        listOf(
            DeckPage("p1", "Main", listOf(DeckSection("s1", "Scenes", SectionStyle.GRID, listOf(button("a"), button("b"), button("c"))))),
            DeckPage("p2", "Model", listOf(DeckSection("s2", null, SectionStyle.GRID, emptyList()))),
        ),
    )

    private fun DeckLayout.ids(sectionId: String) = findSection(sectionId)!!.buttons.map { it.id }

    @Test
    fun locateFindsPageSectionAndIndex() {
        val location = layout.locate("b")!!
        assertEquals("p1", location.page.id)
        assertEquals("s1", location.section.id)
        assertEquals(1, location.index)
        assertNull(layout.locate("missing"))
    }

    @Test
    fun removeThenInsertPutsTheButtonBackInPlace() {
        val location = layout.locate("b")!!
        val removed = layout.removeButton("b")
        assertEquals(listOf("a", "c"), removed.ids("s1"))
        val restored = removed.insertButton(location.section.id, location.index, location.button)
        assertEquals(listOf("a", "b", "c"), restored.ids("s1"))
    }

    @Test
    fun insertWithAHugeIndexAppends() {
        assertEquals(listOf("x"), layout.insertButton("s2", Int.MAX_VALUE, button("x")).ids("s2"))
        assertEquals(listOf("a", "b", "c", "x"), layout.insertButton("s1", Int.MAX_VALUE, button("x")).ids("s1"))
    }

    @Test
    fun replaceSwapsByIdOnly() {
        val changed = button("b").copy(label = "Renamed")
        val updated = layout.replaceButton(changed)
        assertEquals("Renamed", updated.locate("b")!!.button.label)
        assertEquals("Button a", updated.locate("a")!!.button.label)
    }

    @Test
    fun pagesCanBeAddedRenamedAndRemovedButNotTheLastOne() {
        val page = newPage("New page")
        assertEquals(1, page.sections.size)
        val added = layout.addPage(page)
        assertEquals(listOf("Main", "Model", "New page"), added.pages.map { it.name })
        assertEquals("Extras", added.renamePage(page.id, "Extras").pages.last().name)
        assertEquals(listOf("Main", "Model"), added.removePage(page.id).pages.map { it.name })

        val single = DeckLayout(listOf(layout.pages.first()))
        assertSame(single, single.removePage("p1"))
    }

    @Test
    fun buttonUsingMatchesByIdOrByNameWithoutAnId() {
        val gaming = ActionInfo(id = "g-1", name = "Deck - Scene Gaming")
        val withId = layout.replaceButton(button("a", actionId = "g-1", actionName = "old name"))
        assertEquals("a", withId.buttonUsing(gaming)?.id)

        val byName = layout.replaceButton(button("c", actionName = "Deck - Scene Gaming"))
        assertEquals("c", byName.buttonUsing(gaming)?.id)

        // A button with a different id isn't matched by name.
        val otherId = layout.replaceButton(button("b", actionId = "x", actionName = "Deck - Scene Gaming"))
        assertNull(otherId.buttonUsing(gaming))
    }
}
