package com.example.pinkstream.data.deck

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutBackupTest {

    @Test
    fun defaultLayoutSurvivesARoundTrip() {
        val layout = DefaultLayout.create()
        val text = LayoutBackup.encode(layout)
        assertTrue("pretty-printed for reading on a PC", text.contains("\n"))
        assertEquals(layout, LayoutBackup.decode(text))
    }

    @Test
    fun notALayoutIsRejected() {
        assertNull(LayoutBackup.decode("hello"))
        assertNull(LayoutBackup.decode("""{"something":"else"}"""))
        assertNull(LayoutBackup.decode(""))
    }

    @Test
    fun layoutWithoutPagesIsRejected() {
        assertNull(LayoutBackup.decode("""{"pages":[]}"""))
    }

    @Test
    fun duplicateIdsAreRejected() {
        val layout = DefaultLayout.create()
        val firstButton = layout.pages[0].sections[1].buttons[0]
        val copied = layout.insertButton(layout.pages[0].sections[1].id, 0, firstButton)
        assertNull(LayoutBackup.decode(LayoutBackup.encode(copied)))
    }

    @Test
    fun blankIdsAreRejected() {
        val layout = DefaultLayout.create()
        val page = layout.pages[0]
        val broken = layout.copy(pages = listOf(page.copy(id = "")) + layout.pages.drop(1))
        assertNull(LayoutBackup.decode(LayoutBackup.encode(broken)))
    }

    @Test
    fun unknownFieldsFromANewerVersionAreIgnored() {
        val text = LayoutBackup.encode(DefaultLayout.create()).replaceFirst("{", """{"futureField": 1,""")
        assertEquals(DefaultLayout.create().pages.size, LayoutBackup.decode(text)?.pages?.size)
    }
}
