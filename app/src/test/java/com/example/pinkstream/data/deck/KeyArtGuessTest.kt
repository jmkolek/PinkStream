package com.example.pinkstream.data.deck

import com.example.pinkstream.ui.icons.DeckKeyArt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KeyArtGuessTest {

    @Test
    fun theDefaultActionsGetTheirArt() {
        assertEquals(DeckKeyArt.STARTING_SOON, guessKeyArt("Deck - Scene Starting"))
        assertEquals(DeckKeyArt.GAMING, guessKeyArt("Deck - Scene Gaming"))
        assertEquals(DeckKeyArt.JUST_CHATTING, guessKeyArt("Deck - Scene Just chatting"))
        assertEquals(DeckKeyArt.BRB, guessKeyArt("Deck - Scene BRB"))
        assertEquals(DeckKeyArt.ENDING, guessKeyArt("Deck - Scene Ending"))
        assertEquals(DeckKeyArt.PRIVACY, guessKeyArt("Deck - Privacy"))
        assertEquals(DeckKeyArt.MIC, guessKeyArt("Deck - Mic Toggle"))
        assertEquals(DeckKeyArt.DESKTOP_AUDIO, guessKeyArt("Deck - Desktop Toggle"))
        assertEquals(DeckKeyArt.MUSIC, guessKeyArt("Deck - Music Toggle"))
        assertEquals(DeckKeyArt.APPLAUSE, guessKeyArt("Deck - Applause"))
        assertEquals(DeckKeyArt.HAPPY, guessKeyArt("Deck - VTS Happy"))
        assertEquals(DeckKeyArt.BLUSH, guessKeyArt("Deck - VTS Blush"))
        assertEquals(DeckKeyArt.RESET_MODEL, guessKeyArt("Deck - VTS Reset"))
    }

    @Test
    fun longerNamesWinAndWordsMustBeWhole() {
        assertEquals(DeckKeyArt.END_STREAM, guessKeyArt("End stream"))
        assertEquals(DeckKeyArt.AD_BREAK, guessKeyArt("Run ad break"))
        assertEquals(DeckKeyArt.RING_LIGHT, guessKeyArt("Ring light toggle"))
        assertEquals(DeckKeyArt.CAMERA, guessKeyArt("Camera"))           // not "cam" → Webcam
        assertEquals(DeckKeyArt.WEBCAM, guessKeyArt("Face cam"))
        assertNull(guessKeyArt("Deck - Test"))
        assertNull(guessKeyArt("Macro"))                                 // "mic" isn't a whole word here
    }
}
