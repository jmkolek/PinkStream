package com.example.pinkstream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/** A filter chip ("Stream Deck", "All groups"): 36 dp pill inside a 44 dp touch area. */
@Composable
fun DeckChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DeckTheme.colors
    Box(
        modifier
            .height(44.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .height(36.dp)
                .clip(DeckShapes.pill)
                .background(if (selected) colors.ink else colors.surface)
                .border(1.dp, if (selected) Color.Transparent else colors.hairline, DeckShapes.pill)
                .padding(horizontal = 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label,
                style = if (selected) DeckType.pillLarge else DeckType.pillLarge.copy(fontWeight = FontWeight.Medium),
                // Selected: surface-coloured text on ink (white by day, dark plum at night).
                color = if (selected) colors.surface else colors.inkSecondary,
            )
        }
    }
}
