package com.example.pinkstream.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {
    @Test
    fun dayAndNightIgnoreThePhone() {
        assertEquals(false, ThemeMode.DAY.isNight(systemInDarkTheme = true))
        assertEquals(true, ThemeMode.NIGHT.isNight(systemInDarkTheme = false))
    }

    @Test
    fun autoFollowsThePhone() {
        assertEquals(true, ThemeMode.AUTO.isNight(systemInDarkTheme = true))
        assertEquals(false, ThemeMode.AUTO.isNight(systemInDarkTheme = false))
    }
}
