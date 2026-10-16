package com.example.pinkstream.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/** "SCENES"-style label: pass sentence case, it's uppercased here. 22 dp above, 11 dp below. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = DeckType.sectionLabel,
        color = DeckTheme.colors.inkSecondary,
        modifier = modifier
            .padding(top = 22.dp, bottom = 11.dp)
            .semantics { heading() },
    )
}
