package com.example.pinkstream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/**
 * Columns 3 / 4 style picker: a mist track with the chosen option raised on a
 * white pill. TalkBack reads it as a group of radio buttons.
 */
@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    optionPadding: Dp = 11.dp,
    optionMinWidth: Dp = 0.dp,
) {
    val colors = DeckTheme.colors
    Row(
        modifier
            .clip(DeckShapes.segmentTrack)
            .background(colors.boxMist.container)
            .padding(3.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                Modifier
                    .height(38.dp)
                    .widthIn(min = optionMinWidth)
                    .softShadow(if (isSelected) colors.segmentShadow else Color.Transparent, 2.dp, DeckShapes.segmentOption)
                    .clip(DeckShapes.segmentOption)
                    .background(if (isSelected) colors.surface else Color.Transparent)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(option) })
                    .padding(horizontal = optionPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label(option),
                    style = if (isSelected) DeckType.pillLarge else DeckType.pillLarge.copy(fontWeight = FontWeight.Medium),
                    color = if (isSelected) colors.ink else colors.inkSecondary,
                )
            }
        }
    }
}
