package com.example.pinkstream.ui.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pinkstream.R
import com.example.pinkstream.ui.components.CircleIconButton
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/** Stand-in for screens that later milestones build. */
@Composable
fun PlaceholderScreen(title: String, note: String, onBack: () -> Unit) {
    val colors = DeckTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 24.dp),
    ) {
        CircleIconButton(icon = R.drawable.ic_deck_chevron_left, contentDescription = "Back", onClick = onBack)
        Spacer(Modifier.height(14.dp))
        Text(title, style = DeckType.screenTitle, color = colors.ink)
        Spacer(Modifier.height(12.dp))
        Text(note, style = DeckType.caption, color = colors.inkSecondary)
    }
}
