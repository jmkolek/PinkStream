package com.example.pinkstream.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.pinkstream.R
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.TileColor

// The order in the mockup: the pastels first, then the two solid colours.
private val SWATCH_ORDER = listOf(TileColor.ROSE, TileColor.LILAC, TileColor.PEACH, TileColor.MIST, TileColor.BERRY, TileColor.PLUM)
private val SOLID = setOf(TileColor.BERRY, TileColor.PLUM)

/** The six tile colours as 44 dp circles spread across the row. */
@Composable
fun ColorSwatches(selected: TileColor, onSelect: (TileColor) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SWATCH_ORDER.forEach { color -> Swatch(color, selected = color == selected, onClick = { onSelect(color) }) }
    }
}

@Composable
private fun Swatch(color: TileColor, selected: Boolean, onClick: () -> Unit) {
    val colors = DeckTheme.colors
    val palette = colors.tile(color)
    val solid = color in SOLID
    val ringColor = colors.primary
    val gapColor = colors.background
    val innerRing = colors.ink.copy(alpha = 0.08f)
    Box(
        Modifier
            .size(44.dp)
            // Selected: a 2 dp gap in the page colour, then a 2 dp rose ring, drawn outside the circle.
            .drawBehind {
                if (selected) {
                    drawCircle(ringColor, radius = size.minDimension / 2 + 4.dp.toPx())
                    drawCircle(gapColor, radius = size.minDimension / 2 + 2.dp.toPx())
                }
                drawCircle(palette.container)
                // Pastels get a faint inner ring so they don't melt into the page.
                if (!solid) drawCircle(innerRing, radius = size.minDimension / 2 - 0.5.dp.toPx(), style = Stroke(1.dp.toPx()))
            }
            .clip(CircleShape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = color.name.lowercase().replaceFirstChar { it.uppercase() } },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                painterResource(R.drawable.ic_deck_check),
                contentDescription = null,
                // White (the tile's own content colour) on Berry and Plum, rose on the pastels.
                tint = if (solid) palette.icon else colors.primary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
