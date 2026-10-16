package com.example.pinkstream

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pinkstream.ui.navigation.DeckNavHost
import com.example.pinkstream.ui.theme.PinkStreamTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Draw behind the status and navigation bars; each screen pads itself with the
        // system bar insets. SystemBarsFollow below matches the bar icons to the theme.
        enableEdgeToEdge()
        val settings = (application as StreamDeckApp).container.settings
        setContent {
            val themeModeFlow = remember { settings.settings.map { it.themeMode }.distinctUntilChanged() }
            // null until DataStore has answered (a few ms). Drawing nothing until then
            // avoids flashing day colours for a frame when Night is saved.
            val themeMode by themeModeFlow.collectAsStateWithLifecycle(initialValue = null)
            themeMode?.let { mode ->
                SystemBarsFollow(night = mode.isNight(isSystemInDarkTheme()))
                PinkStreamTheme(themeMode = mode) {
                    DeckNavHost()
                }
            }
        }
    }

    /** Light bar icons on the night theme, dark ones by day, even when that differs from the phone. */
    @Composable
    private fun SystemBarsFollow(night: Boolean) {
        DisposableEffect(night) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { night },
                // The same scrims enableEdgeToEdge() uses by default (only shown with 3-button navigation).
                navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { night },
            )
            onDispose {}
        }
    }

    private companion object {
        val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
