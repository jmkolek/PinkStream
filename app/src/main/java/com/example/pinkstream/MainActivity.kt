package com.example.pinkstream

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pinkstream.ui.navigation.DeckNavHost
import com.example.pinkstream.ui.theme.PinkStreamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Draw behind the status and navigation bars; each screen pads itself with the
        // system bar insets. The app is always light, so the bars always get dark icons,
        // even when the phone itself is in dark mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            // The scrim enableEdgeToEdge() uses by default (only shown with 3-button navigation).
            navigationBarStyle = SystemBarStyle.light(LIGHT_SCRIM, LIGHT_SCRIM),
        )
        setContent {
            PinkStreamTheme {
                DeckNavHost()
            }
        }
    }

    private companion object {
        val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
    }
}
