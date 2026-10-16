package com.example.pinkstream.ui.theme

/** Day, night, or follow the phone's dark theme setting. */
enum class ThemeMode {
    DAY, NIGHT, AUTO;

    fun isNight(systemInDarkTheme: Boolean): Boolean = when (this) {
        DAY -> false
        NIGHT -> true
        AUTO -> systemInDarkTheme
    }
}
