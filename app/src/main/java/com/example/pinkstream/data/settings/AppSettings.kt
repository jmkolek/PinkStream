package com.example.pinkstream.data.settings

import com.example.pinkstream.ui.theme.ThemeMode

/** Where to find Streamer.bot. */
data class ConnectionTarget(val host: String, val port: Int, val password: String)

/** Everything on the Settings screen. The defaults are what a fresh install starts with. */
data class AppSettings(
    val host: String = ConnectionDefaults.HOST,
    val port: Int = ConnectionDefaults.PORT,
    val password: String = "",
    val keepScreenOn: Boolean = true,
    val vibrate: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val columns: Int = DEFAULT_COLUMNS,
) {
    val connection: ConnectionTarget
        get() = ConnectionTarget(host, port, password)

    companion object {
        const val DEFAULT_COLUMNS = 3
        val COLUMN_CHOICES = listOf(3, 4)
    }
}
