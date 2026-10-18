package com.example.pinkstream.data.deck

import com.example.pinkstream.ui.icons.DeckKeyArt
import com.example.pinkstream.ui.icons.PageIcon
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutMigrationTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Test
    fun anOldLayoutIsFlattenedKeepingIdsActionsAndObsNames() {
        val decoded = LayoutMigration.decode(json, OLD_LAYOUT)!!
        assertTrue(decoded.migrated)
        val (main, model) = decoded.layout.pages
        assertEquals("main", main.id)
        assertEquals(PageIcon.GRID, main.icon)
        assertEquals(PageIcon.SMILE, model.icon)

        // The hero first, then the sections in order.
        assertEquals(listOf("mic", "gaming", "happy"), main.keys.map { it.id })
        val (mic, gaming, happy) = main.keys
        assertEquals(DeckKey("mic", "Microphone", DeckKeyArt.MIC, "m-1", "Deck - Mic Toggle", stateInput = "Mic/Aux"), mic)
        assertEquals(DeckKeyArt.GAMING, gaming.art)
        assertEquals("Tajniaks - Japan Theme InGame Scene", gaming.onAirScene)
        assertTrue(gaming.confirm)
        assertEquals(DeckKeyArt.HAPPY, happy.art)
        assertEquals(emptyList<DeckKey>(), model.keys)
    }

    @Test
    fun artComesFromTheLabelThenTheActionThenTheOldIcon() {
        assertEquals(DeckKeyArt.BRB, LayoutMigration.artFor("BRB", null, "BOLT"))
        assertEquals(DeckKeyArt.BLUSH, LayoutMigration.artFor("Cute", "Deck - VTS Blush", "BOLT"))
        assertEquals(DeckKeyArt.CREATE_CLIP, LayoutMigration.artFor("Save that", "Deck - Thing", "SCISSORS"))
        assertEquals(DeckKeyArt.HIGHLIGHT, LayoutMigration.artFor("Something", "Deck - Test", "NOT_AN_ICON"))
    }

    @Test
    fun aCurrentLayoutIsNotMigrated() {
        val layout = DefaultLayout.create()
        val decoded = LayoutMigration.decode(json, json.encodeToString(DeckLayout.serializer(), layout))!!
        assertFalse(decoded.migrated)
        assertEquals(layout, decoded.layout)
    }

    @Test
    fun neitherShapeIsNull() {
        assertNull(LayoutMigration.decode(json, "{ not json"))
        assertNull(LayoutMigration.decode(json, """{"pages":[{"id":"p","name":"Main"}]}"""))
    }

    companion object {
        /** A layout as the app saved it before the redesign (M3–M6). */
        val OLD_LAYOUT = """
            {"pages":[
              {"id":"main","name":"Main","sections":[
                {"id":"s0","style":"HERO","buttons":[
                  {"id":"mic","label":"Microphone","icon":"MIC","color":"ROSE","actionId":"m-1","actionName":"Deck - Mic Toggle","stateInput":"Mic/Aux"}
                ]},
                {"id":"s1","title":"Scenes","style":"GRID","buttons":[
                  {"id":"gaming","label":"Gaming","icon":"GAMEPAD","color":"ROSE","actionName":"Deck - Scene Gaming","onAirScene":"Tajniaks - Japan Theme InGame Scene","confirm":true}
                ]},
                {"id":"s2","title":"Model","style":"GRID","buttons":[
                  {"id":"happy","label":"Happy","icon":"SMILE","color":"LILAC","actionName":"Deck - VTS Happy"}
                ]}
              ]},
              {"id":"model","name":"Model","sections":[{"id":"s3","title":"Expressions","style":"GRID","buttons":[]}]}
            ]}
        """.trimIndent()
    }
}
