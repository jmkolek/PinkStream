package com.example.pinkstream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pinkstream.ui.navigation.DeckNavHost
import com.example.pinkstream.ui.theme.PinkStreamTheme
import com.example.pinkstream.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Draw behind the status and navigation bars; each screen pads itself with the
        // system bar insets. Bar icons follow the phone's dark theme, which matches AUTO.
        enableEdgeToEdge()
        setContent {
            PinkStreamTheme(themeMode = ThemeMode.AUTO) {
                DeckNavHost()
            }
        }
    }
}
