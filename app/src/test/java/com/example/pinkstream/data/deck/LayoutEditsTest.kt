package com.example.pinkstream.data.deck

import com.example.pinkstream.data.streamerbot.ActionInfo
import com.example.pinkstream.ui.icons.DeckKeyArt
import com.example.pinkstream.ui.icons.PageIcon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class LayoutEditsTest {

    private fun key(id: String, actionId: String? = null, actionName: String? = null) =
        DeckKey(id, "Key $id", DeckKeyArt.HIGHLIGHT, actionId = actionId, actionName = actionName)

    private val layout = DeckLayout(
        listOf(
            DeckPage("p1", "Main", PageIcon.GRID, listOf(key("a"), key("b"), key("c"))),
            DeckPage("p2", "Model", PageIcon.SMILE, emptyList()),
        ),
    )

    private fun DeckLayout.ids(pageId: String) = findPage(pageId)!!.keys.map { it.id }

    @Test
    fun locateFindsPageAndIndex() {
        val location = layout.locate("b")!!
        assertEquals("p1", location.page.id)
        assertEquals(1, location.index)
        assertNull(layout.locate("missing"))
    }

    @Test
    fun removeThenInsertPutsTheKeyBackInPlace() {
        val location = layout.locate("b")!!
        val removed = layout.removeKey("b")
        assertEquals(listOf("a", "c"), removed.ids("p1"))
        val restored = removed.insertKey(location.page.id, location.index, location.key)
        assertEquals(listOf("a", "b", "c"), restored.ids("p1"))
    }

    @Test
    fun insertWithAHugeIndexAppends() {
        assertEquals(listOf("x"), layout.insertKey("p2", Int.MAX_VALUE, key("x")).ids("p2"))
        assertEquals(listOf("a", "b", "c", "x"), layout.insertKey("p1", Int.MAX_VALUE, key("x")).ids("p1"))
    }

    @Test
    fun replaceSwapsByIdOnly() {
        val changed = key("b").copy(label = "Renamed")
        val updated = layout.replaceKey(changed)
        assertEquals("Renamed", updated.locate("b")!!.key.label)
        assertEquals("Key a", updated.locate("a")!!.key.label)
    }

    @Test
    fun pagesCanBeAddedChangedAndRemovedButNotTheLastOne() {
        val page = newPage("New page", PageIcon.STAR)
        assertEquals(emptyList<DeckKey>(), page.keys)
        val added = layout.addPage(page)
        assertEquals(listOf("Main", "Model", "New page"), added.pages.map { it.name })
        val changed = added.updatePage(page.id, "Extras", PageIcon.SPARKLES).pages.last()
        assertEquals("Extras", changed.name)
        assertEquals(PageIcon.SPARKLES, changed.icon)
        assertEquals(listOf("Main", "Model"), added.removePage(page.id).pages.map { it.name })

        val single = DeckLayout(listOf(layout.pages.first()))
        assertSame(single, single.removePage("p1"))
    }

    @Test
    fun pagesMoveOnePlaceAndStayInTheDock() {
        assertEquals(listOf("p2", "p1"), layout.movePage("p1", 1).pages.map { it.id })
        assertEquals(listOf("p2", "p1"), layout.movePage("p2", -1).pages.map { it.id })
        assertSame(layout, layout.movePage("p1", -1))     // already first
        assertSame(layout, layout.movePage("p2", 1))      // already last
        assertSame(layout, layout.movePage("missing", 1))
    }

    @Test
    fun keysMoveWithinTheirPage() {
        assertEquals(listOf("b", "c", "a"), layout.moveKey("p1", 0, 2).ids("p1"))
        assertEquals(listOf("c", "a", "b"), layout.moveKey("p1", 2, 0).ids("p1"))
        assertSame(layout, layout.moveKey("p1", 1, 1))
        assertSame(layout, layout.moveKey("p1", 0, 3))        // past the last key
        assertSame(layout, layout.moveKey("p2", 0, 0))        // empty page
        assertSame(layout, layout.moveKey("missing", 0, 1))
    }

    @Test
    fun theDockHoldsAtMostFivePages() {
        var full = layout
        repeat(MAX_PAGES) { full = full.addPage(newPage("Page $it", PageIcon.STAR)) }
        assertEquals(MAX_PAGES, full.pages.size)
        assertSame(full, full.addPage(newPage("One too many", PageIcon.STAR)))
    }

    @Test
    fun keyUsingMatchesByIdOrByNameWithoutAnId() {
        val gaming = ActionInfo(id = "g-1", name = "Deck - Scene Gaming")
        val withId = layout.replaceKey(key("a", actionId = "g-1", actionName = "old name"))
        assertEquals("a", withId.keyUsing(gaming)?.id)

        val byName = layout.replaceKey(key("c", actionName = "Deck - Scene Gaming"))
        assertEquals("c", byName.keyUsing(gaming)?.id)

        // A key with a different id isn't matched by name.
        val otherId = layout.replaceKey(key("b", actionId = "x", actionName = "Deck - Scene Gaming"))
        assertNull(otherId.keyUsing(gaming))
    }
}
