package com.example.pinkstream.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.pinkstream.R
import com.example.pinkstream.ui.components.DeckBottomSheet
import com.example.pinkstream.ui.icons.DeckIcon
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType

/** Five quick icon choices plus "More icons", spread across the row. */
@Composable
fun IconRow(icons: List<DeckIcon>, selected: DeckIcon, onSelect: (DeckIcon) -> Unit, onMore: () -> Unit) {
    Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.SpaceBetween) {
        icons.forEach { icon -> IconChoice(icon, selected = icon == selected, onClick = { onSelect(icon) }) }
        MoreIconsChoice(onMore)
    }
}

/** Every DeckIcon in a grid; picking one closes the sheet. */
@Composable
fun MoreIconsSheet(selected: DeckIcon, onSelect: (DeckIcon) -> Unit, onDismiss: () -> Unit) {
    DeckBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 24.dp)) {
            Text("Choose an icon", style = DeckType.sheetTitle, color = DeckTheme.colors.ink)
            Spacer(Modifier.height(18.dp))
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DeckIcon.entries.chunked(ICONS_PER_ROW).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        row.forEach { icon ->
                            IconChoice(icon, selected = icon == selected, onClick = { onSelect(icon) })
                        }
                        // Keep a short last row lined up with the columns above.
                        repeat(ICONS_PER_ROW - row.size) { Spacer(Modifier.size(46.dp)) }
                    }
                }
            }
        }
    }
}

private const val ICONS_PER_ROW = 6

/** "EYE_OFF" → "Eye off", for TalkBack. */
private val DeckIcon.spokenName: String
    get() = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

@Composable
private fun IconChoice(icon: DeckIcon, selected: Boolean, onClick: () -> Unit) {
    val colors = DeckTheme.colors
    Box(
        Modifier
            .size(46.dp)
            .clip(DeckShapes.iconChoice)
            .background(if (selected) colors.primary else colors.surface)
            .border(1.dp, if (selected) Color.Transparent else colors.hairline, DeckShapes.iconChoice)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = icon.spokenName },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(icon.drawable),
            contentDescription = null,
            tint = if (selected) colors.onPrimary else colors.inkSecondary,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun MoreIconsChoice(onClick: () -> Unit) {
    val colors = DeckTheme.colors
    Box(
        Modifier
            .size(46.dp)
            .clip(DeckShapes.iconChoice)
            .background(colors.tileMist.container)
            .selectable(selected = false, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(R.drawable.ic_deck_dots), contentDescription = "More icons", tint = colors.inkSecondary, modifier = Modifier.size(22.dp))
    }
}
